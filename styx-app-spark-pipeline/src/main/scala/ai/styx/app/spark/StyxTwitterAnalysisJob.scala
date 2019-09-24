package ai.styx.app.spark

import java.sql.Timestamp

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.{Tweet, TweetWord}
import ai.styx.domain.utils.{Column, ColumnType}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.joda.time.DateTime
import ai.styx.frameworks.ignite.{EmbeddedIgnite, IgniteFactory}
import ai.styx.frameworks.interfaces.DatabaseWriter

object StyxTwitterAnalysisJob extends App with Logging with EmbeddedIgnite {
  LOG.info("Spark version " + org.apache.spark.SPARK_VERSION)

  val config = Configuration.load()
  val minimumWordLength = 5
  val wordsToIgnore = Array("would", "could", "should", "sometimes", "maybe", "perhaps", "nothing", "please", "today", "twitter", "everyone", "people", "think", "where", "about", "still", "youre")

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
    .map(json => {Tweet.fromString(json)}) // convert to domain class Tweet
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
      // sliding window of 60 seconds, evaluated every 30 seconds
      window($"created_at", "60 seconds", "30 seconds"),
      $"word")
    .count()

  // windowedTweets is a sql.DataFrame

  // cache the word counts per window



  val output = windowedTweets.writeStream.format("console").start()

  output.awaitTermination()

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
    dbWriter.createTable("top_tweets", None, Some(List(Column("window", ColumnType.TIMESTAMP), Column("word", ColumnType.TEXT), Column("count", ColumnType.INT))))
    LOG.info("Database tables created")
  }
}
