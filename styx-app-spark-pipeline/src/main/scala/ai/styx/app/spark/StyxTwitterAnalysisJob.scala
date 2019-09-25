package ai.styx.app.spark

import java.sql.Timestamp

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.{Tweet, TweetWindowTrend, TweetWord}
import ai.styx.domain.utils.{Column, ColumnType}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.joda.time.DateTime
import ai.styx.frameworks.ignite.{EmbeddedIgnite, IgniteFactory}
import ai.styx.frameworks.interfaces.DatabaseWriter

object StyxTwitterAnalysisJob extends App with Logging {
  LOG.info("Spark version " + org.apache.spark.SPARK_VERSION)

  val config = Configuration.load()
  val minimumWordLength = 5
  val wordsToIgnore = Array("would", "could", "should", "sometimes", "maybe", "perhaps", "nothing", "please", "today", "twitter", "everyone", "people", "think", "where", "about", "still", "youre")
  val columns = List(Column("id", ColumnType.TEXT), Column("windowStart", ColumnType.TIMESTAMP), Column("windowEnd", ColumnType.TIMESTAMP), Column("word", ColumnType.TEXT), Column("count", ColumnType.INT))

  var id = 1
  var windowCount = 0

  // connect to Spark
  val conf = new SparkConf()
    .setMaster("local[2]")
    .setAppName("Styx")

  val spark = SparkSession
    .builder
    .config(conf)
    .getOrCreate()

  import spark.sqlContext.implicits._

  // connect to Kafka: subscribe to topic
  val df = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "tweets")
    .option("startingOffsets", "earliest")
    .load()

  // connect to Ignite
  val dbWriter: DatabaseWriter = new IgniteFactory(config.igniteConfig.url).createWriter.asInstanceOf[DatabaseWriter]
  createTables(dbWriter)

  ///// part 1a CEP: count the words per period /////

  val tweetStream = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)").as("kv") // get key/value pair from Kafka
    .map(kv => kv.getString(1)) // get string value
    .map(json => {
      Tweet.fromString(json)
    }) // convert to domain class Tweet
    .filter(_ != null)

    // create multiple TweetWord objects from 1 Tweet object. Keep the Timestamp, but split the text in words
    .flatMap(tweet => {
      val words = tweet.text
        // remove special characters & new lines
        .replaceAll("[~!@#$^%&*\\\\(\\\\)_+={}\\\\[\\\\]|;:\\\"'<,>.?`/\\n\\\\\\\\-]", "")
        // convert to lower case
        .toLowerCase()
        // create words
        .split("[ \\t]+")

      words.map(word => TweetWord(tweet.created_at, word))
    })
    .filter(tw => !wordsToIgnore.contains(tw.word) && tw.word.length >= minimumWordLength)

  val windowedTweets = tweetStream
    .withWatermark("created_at", "1 second")
    .groupBy(
      // sliding window of 2 seconds, evaluated every 1 second
      window($"created_at", "2 seconds", "1 second"),
      $"word")
    .agg(count("word") as "count")  // also possible: .count(), but that doesn't preserve the window details
    .select("window.start", "window.end", "word", "count")
    .filter("count > 2")

  val igniteSink = windowedTweets
    .map(row => {
      id = id + 1
      TweetWindowTrend(
        id.toString,
        row.getAs[Timestamp]("start"),
        row.getAs[Timestamp]("end"),
        row.getAs[String]("word"),
        row.getAs[Long]("count"))
    })
    .map(t => {
      dbWriter.putDomainEntity("top_tweets", t)
      t
    })

  // TODO: compare windows
 // igniteSink.map(t => t)

  val output = igniteSink.writeStream.format("console").start()
  output.awaitTermination()

  // Have all the aggregates in an in-memory table
  //  aggDF
  //    .writeStream
  //    .queryName("aggregates")    // this query name will be the table name
  //    .outputMode("complete")
  //    .format("memory")
  //    .start()
  //

  ///// part 1b CEP: look at 2 periods (e.g. hours) and calculate slope, find top 5 /////

  // TODO: use state, mapGroupsWithState

  ///// #2: ML, get notification /////

  ///// #3: Notification /////


  // sink the data to Kafka
  //  val ds = df
  //    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
  //    .writeStream
  //    .format("kafka")
  //    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  //    .option("topic", "topic1")
  //    .start()

  def round(ts: Timestamp): Timestamp = {
    val date = new DateTime(ts)

    new Timestamp(date.year().get, date.monthOfYear().get, date.dayOfMonth().get, date.hourOfDay().get, date.minuteOfHour().get, 0, 0)
  }

  def createTables(dbWriter: DatabaseWriter) = {
    dbWriter.deleteTable("top_tweets")
    dbWriter.createTable("top_tweets", None, Some(columns))
    LOG.info("Database tables created")
  }
}
