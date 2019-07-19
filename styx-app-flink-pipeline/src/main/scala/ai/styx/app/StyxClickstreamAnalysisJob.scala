package ai.styx.app

import java.util.Properties

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.Click
import ai.styx.usecases.clickstream.{CategoryCount, CategoryCountWindowFunction, CategorySumWindowFunction, ClickTimestampAndWatermarkGenerator}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.joda.time.DateTime

object StyxClickstreamAnalysisJob extends App with Logging {
  implicit val config: Configuration = Configuration.load()

  // set up Flink
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

  // load the data
  // TODO: refactor, similar as Twitter analysis
  val properties = new Properties()
  properties.setProperty("bootstrap.servers", config.kafkaConfig.bootstrapServers)
  properties.setProperty("group.id", config.kafkaConfig.groupId)

  implicit val typeInfoString = TypeInformation.of(classOf[String])
  implicit val typeInfoOptionString = TypeInformation.of(classOf[Option[String]])
  implicit val typeInfoClick = TypeInformation.of(classOf[Click])
  implicit val typeInfoClickInt = TypeInformation.of(classOf[(Click, Int)])
  implicit val typeInfoStringInt = TypeInformation.of(classOf[(String, Int)])
  implicit val typeInfoCategoryCount = TypeInformation.of(classOf[CategoryCount])
  implicit val typeInfoListString = TypeInformation.of(classOf[List[CategoryCount]])

  val stream = env
    .addSource(new FlinkKafkaConsumer[String](config.kafkaConfig.rawDataTopic, new SimpleStringSchema(), properties))
    .map(Click.fromString(_))
    // set event timestamp
    .assignTimestampsAndWatermarks(new ClickTimestampAndWatermarkGenerator).name("Getting event time")
    .filter(_.category.isDefined)
    .map(_.category.get)
    .map(s => Tuple2(s, 1)).name("Creating tuples")
    // group by word
    .keyBy(_._1)
    // group by period
    .timeWindow(Time.seconds(config.sparkConfig.windowDuration))
    // count the words per period
    .apply(new CategoryCountWindowFunction()).name("Counting categories")

  stream
    .windowAll(SlidingEventTimeWindows.of(Time.seconds(config.sparkConfig.windowDuration), Time.seconds(config.sparkConfig.slideDuration.toLong)))
    .apply(new CategorySumWindowFunction(10)).name("Top 10 categories per window")
    .addSink{listPerPeriod =>
      if (listPerPeriod.nonEmpty) {
        val dt = new DateTime(listPerPeriod.head.timeStamp)
        val s = s"## ============ WINDOW START : ${dt.toString("dd-MM-yyyy hh:mm:sss.SSS")} =========="
        LOG.info((s :: listPerPeriod.map(s => f"## Count of ${s.count}%5d : ${s.word}")).mkString("\n"))
      }}.name("Printing results")

  env.execute(config.sparkConfig.appName)
}
