package ai.styx.app.spark

import java.sql.Timestamp

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.{Tweet, TweetWindowTrend, TweetWord}
import ai.styx.domain.utils.{Column, ColumnType}
import ai.styx.frameworks.ignite.IgniteFactory
import ai.styx.frameworks.interfaces.{DatabaseFetcher, DatabaseWriter}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object StyxTwitterAnalysisJob extends App with Logging {
  LOG.info("Spark version " + org.apache.spark.SPARK_VERSION)

  val config = Configuration.load()
  val minimumWordLength = 5
  val wordsToIgnore = Array("would", "could", "should", "sometimes", "maybe", "perhaps", "nothing", "please", "today", "twitter", "everyone", "people", "think", "where", "about", "still", "youre")
  val columns = List(Column("id", ColumnType.TEXT), Column("windowStart", ColumnType.TIMESTAMP), Column("windowEnd", ColumnType.TIMESTAMP), Column("word", ColumnType.TEXT), Column("count", ColumnType.INT))

  val minimumWordCount = 20
  val windowSizeInSeconds = 10
  val slideSizeInSeconds = 2
  val watermarkSeconds = 1

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
    .option("startingOffsets", "latest")
    .load()

  // connect to Ignite
  val dbFactory = new IgniteFactory(config.igniteConfig.url)
  val dbWriter: DatabaseWriter = dbFactory.createWriter
  val dbFetcher: DatabaseFetcher = dbFactory.createFetcher

  //createTables(dbWriter)

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

  /**
    * If the timestamp is in the first half of the window: 0, else 1
    */
  val calculateWindowPart = udf[Int, Timestamp, Timestamp, Timestamp]((created, start, end) => {
    // ! timestamps of Tweets are rounded in seconds
    // for example:
    // windowSizeInSeconds = 10
    // start window: 15:08:11
    // end window: 15:08:21
    // timestamp: 15:08:12 ==> window 1
    // timestamp: 15:08:18 ==> window 2

    val halftimeMillis = start.getTime + ((end.getTime - start.getTime) / 2)

    if (created.getTime < halftimeMillis) 0 else 1
  })

  val windowedTweets = tweetStream
    .withWatermark("created_at", s"$watermarkSeconds seconds")
    .groupBy(
      // sliding window of 2 seconds, evaluated every 1 second
      window(
        $"created_at",
        s"$windowSizeInSeconds seconds"),
        //s"$slideSizeInSeconds second"),  // use for sliding window
      $"word", calculateWindowPart($"created_at", $"window.start", $"window.end") as "windowPart")
    .agg(count("word") as "count", $"word", $"window.start", $"window.end")
    .sort( asc("window.start"), asc("windowPart"), desc("count"))
    .select("windowPart", "window.start", "window.end", "word", "count")
    .filter(s"count > $minimumWordCount")
    .map(row => {
      TweetWindowTrend(
        null,  // ID will be generated
        row.getAs[Int]("windowPart"),
        row.getAs[Timestamp]("start"),
        row.getAs[Timestamp]("end"),
        row.getAs[String]("word"),
        row.getAs[Long]("count"))
    })

  val trends = windowedTweets

//  val igniteSink = windowedTweets
//    .map(t => {
//      dbWriter.putDomainEntity("top_tweets", t)
//      t
//    })

  // compare the first half of the window by the second one; we can do this by looking at the event time

  val trendsOutput = trends
    .writeStream
    .format("console")
    .outputMode("Complete")
    .start()

  trendsOutput.awaitTermination()

  // delete older windows from cache

  // Have all the aggregates in an in-memory table
  //  aggDF
  //    .writeStream
  //    .queryName("aggregates")    // this query name will be the table name
  //    .outputMode("complete")
  //    .format("memory")
  //    .start()
  //

  ///// part 1b CEP: look at 2 periods (e.g. hours) and calculate slope, find top 5 /////

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

  def createTables(dbWriter: DatabaseWriter) = {
    dbWriter.deleteTable("top_tweets")
    dbWriter.createTable("top_tweets", None, Some(columns))
    LOG.info("Database tables created")
  }

}
