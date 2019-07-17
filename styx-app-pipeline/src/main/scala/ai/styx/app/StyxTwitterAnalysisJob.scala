package ai.styx.app

import java.util.Properties

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.{Trend, Tweet, WordCount}
import ai.styx.usecases.twitter.{TrendsWindowFunction, TweetTimestampAndWatermarkGenerator, WordCountWindowFunction}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.joda.time.DateTime

object StyxTwitterAnalysisJob extends App with Logging {
  // configuration
  implicit val config: Configuration = Configuration.load()
  val minimumWordLength = 5
  val wordsToIgnore = Array("would", "could", "should", "sometimes", "maybe", "perhaps", "nothing", "please", "today", "twitter", "everyone", "people", "think", "where", "about", "still", "youre")
  val evaluationPeriodInSeconds = 3
  val topN = 5
  val dateTimePattern = "yyyy-MM-dd HH:mm:sss"

  LOG.info("Start!")

  // set up Flink
  val startTime = DateTime.now
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

  // connect to Kafka
  val properties = new Properties()
  properties.setProperty("bootstrap.servers", config.kafkaConfig.bootstrapServers)
  properties.setProperty("group.id", config.kafkaConfig.groupId)

  implicit val typeInfo1: TypeInformation[Tweet] = TypeInformation.of(classOf[Tweet])
  implicit val typeInfo2: TypeInformation[Option[Tweet]] = TypeInformation.of(classOf[Option[Tweet]])
  implicit val typeInfo3: TypeInformation[Option[String]] = TypeInformation.of(classOf[Option[String]])
  implicit val typeInfo4: TypeInformation[String] = TypeInformation.of(classOf[String])
  implicit val typeInfo5: TypeInformation[(String, Int)] = TypeInformation.of(classOf[(String, Int)])
  implicit val typeInfo6: TypeInformation[WordCount] = TypeInformation.of(classOf[WordCount])

  val stream = env
    .addSource(new FlinkKafkaConsumer[String](config.kafkaConfig.topic, new SimpleStringSchema(), properties))

  ///// part 1: count the words per period /////
  val wordsStream: DataStream[WordCount] = wordCount(env, stream, minimumWordLength, evaluationPeriodInSeconds, wordsToIgnore)

  ///// part 2: look at 2 periods (e.g. days) and calculate slope, find top 5 /////
  trendsAnalysis(wordsStream, evaluationPeriodInSeconds, topN, dateTimePattern)

  env.execute("Twitter trends")

  LOG.info("Done!")

  private def wordCount(env: StreamExecutionEnvironment, rawData: DataStream[String], minWordL: Int, seconds: Int, ignore: Array[String]): DataStream[WordCount] = {
    rawData
      .map(line => Tweet.parse(line)).filter(_.isDefined).map(_.get).name("Parsing JSON string")
      // set event timestamp
      .assignTimestampsAndWatermarks(new TweetTimestampAndWatermarkGenerator).name("Getting event time")
      .flatMap(_.messageText
        // remove special characters & new lines, and convert to lower case
        .replaceAll("[~!@#$^%&*\\\\(\\\\)_+={}\\\\[\\\\]|;:\\\"'<,>.?`/\\n\\\\\\\\-]", "").toLowerCase()
        // create words
        .split("[ \\t]+")).name("Creating word list")
      .filter(word => !ignore.contains(word) && word.length >= minWordL)
      .map(s => Tuple2(s, 1)).name("Creating tuples")
      // group by word
      .keyBy(_._1)
      // group by period
      .timeWindow(Time.seconds(seconds))
      // count the words per day
      .apply(new WordCountWindowFunction()).name("Counting words")
  }

  private def trendsAnalysis(wordsStream: DataStream[WordCount], seconds: Int, topN: Int, dtPattern: String): DataStreamSink[Map[String, List[Trend]]] = {
    implicit val typeInfo1: TypeInformation[Map[String, List[Trend]]] = TypeInformation.of(classOf[Map[String, List[Trend]]])

    wordsStream
      .windowAll(TumblingEventTimeWindows.of(Time.seconds(seconds * 2)))
      .apply(new TrendsWindowFunction(seconds, dtPattern, topN)).name("Calculating trends")
      .addSink(x => x.foreach { trendPerPeriod =>
        print(trendPerPeriod)
      }).name("Printing results")
  }

  private def print(trendPerPeriod: (String, List[Trend])): Unit = {
    println("### Trending topic of " + trendPerPeriod._1)
    for (i <- trendPerPeriod._2.indices) {
      val trend = trendPerPeriod._2(i)
      println(s" ${i + 1}: ${trend.word.toUpperCase()}, slope=${trend.slope}")
    }
  }
}
