package ai.styx.app.spark

import java.sql.Timestamp

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.{Tweet, TweetWindowTrend, TweetWord}
import ai.styx.domain.utils.{Column, ColumnType}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import ai.styx.frameworks.ignite.IgniteFactory
import ai.styx.frameworks.interfaces.{DatabaseFetcher, DatabaseWriter}

object StyxTwitterAnalysisJob extends App with Logging {
  LOG.info("Spark version " + org.apache.spark.SPARK_VERSION)

  val config = Configuration.load()
  val minimumWordLength = 5
  val wordsToIgnore = Array("would", "could", "should", "sometimes", "maybe", "perhaps", "nothing", "please", "today", "twitter", "everyone", "people", "think", "where", "about", "still", "youre")
  val columns = List(Column("id", ColumnType.TEXT), Column("windowId", ColumnType.INT), Column("windowStart", ColumnType.TIMESTAMP), Column("windowEnd", ColumnType.TIMESTAMP), Column("word", ColumnType.TEXT), Column("count", ColumnType.INT))
  var windowCount = 0L

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
  val dbFactory = new IgniteFactory(config.igniteConfig.url)
  val dbWriter: DatabaseWriter = dbFactory.createWriter
  val dbFetcher: DatabaseFetcher = dbFactory.createFetcher

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
      window(
        $"created_at",
        "2 seconds",
        "1 second"),
      $"word")
    .agg(count("word") as "count")  // also possible: .count(), but that doesn't preserve the window details
    .withColumn("windowId", lit(windowCount))
    .select("windowId", "window.start", "window.end", "word", "count")
    .filter("count > 2")
    .map(row => {
      TweetWindowTrend(
        null,  // ID will be generated
   //     row.getAs[Long]("windowId"),
        row.getAs[Timestamp]("start"),
        row.getAs[Timestamp]("end"),
        row.getAs[String]("word"),
        row.getAs[Long]("count"))
    })

  val igniteSink = windowedTweets
    .map(t => {
      dbWriter.putDomainEntity("top_tweets", t)
      t
    })



  //val output = igniteSink.writeStream.format("console").start()
  //output.awaitTermination()

  val o = igniteSink.toDF().writeStream.foreachBatch { (batchDF: DataFrame, batchId: Long) =>
    //windowCount = batchId
    LOG.info("Batch ID: " + batchId)
    //batchDF.map
    // for each word: find the number of words in previous window
    val items = dbFetcher
      .getItems("windowId", (batchId - 1).toString, "top_tweets")

    LOG.info(s"Found ${items.get.length} words in the previous window")
    //items.map(x => x.map(y => y.map(z => LOG.info(z._1 + "=" + z._2))))

    //"word"
  }.format("console").start()

  o.awaitTermination()

  //val trends = igniteSink.writeStream.foreachBatch()

//  { (batchDF: DataFrame, batchId: Long) =>
//    ""
//  }

  // compare current window to previous one
  val trends = igniteSink.map(w => {
    // for each word: find the number of words in previous window
    val items = dbFetcher
      .getItems("windowId", (windowCount - 1).toString, "top_tweets")

    LOG.info(s"Found ${items.get.length} words in the previous window")
    items.map(x => x.map(y => y.map(z => LOG.info(z._1 + "=" + z._2))))

    "word"
    //items

    // calculate slope / delta

    // sort the deltas to determine top trends
  })

  val trendsOutput = trends.writeStream.format("console").start()
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

  def increment(): Long = {
    windowCount = windowCount + 1
    LOG.info("Window: " + windowCount)
    windowCount
  }
}
