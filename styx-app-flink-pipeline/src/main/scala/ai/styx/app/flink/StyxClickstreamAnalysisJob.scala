package ai.styx.app.flink

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.{Click, ClickDataEnricher}
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringConsumer, KafkaStringProducer}
import ai.styx.usecases.clickstream.{CategoryCount, CategoryCountWindowFunction, CategorySumWindowFunction, ClickTimestampAndWatermarkGenerator}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment


object StyxClickstreamAnalysisJob extends App with Logging {
  implicit val config: Configuration = Configuration.load()

  // set up Flink
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

  implicit val typeInfoString: TypeInformation[String] = TypeInformation.of(classOf[String])
  implicit val typeInfoOptionString: TypeInformation[Option[String]] = TypeInformation.of(classOf[Option[String]])
  implicit val typeInfoClick: TypeInformation[Click] = TypeInformation.of(classOf[Click])
  implicit val typeInfoOptionClick: TypeInformation[Option[Click]] = TypeInformation.of(classOf[Option[Click]])
  implicit val typeInfoClickInt: TypeInformation[(Click, Int)] = TypeInformation.of(classOf[(Click, Int)])
  implicit val typeInfoStringInt: TypeInformation[(String, Int)] = TypeInformation.of(classOf[(String, Int)])
  implicit val typeInfoCategoryCount: TypeInformation[CategoryCount] = TypeInformation.of(classOf[CategoryCount])
  implicit val typeInfoListString: TypeInformation[List[CategoryCount]] = TypeInformation.of(classOf[List[CategoryCount]])

  val producer = KafkaFactory.createStringProducer(config.kafkaProducerProperties).asInstanceOf[KafkaStringProducer]

  // connect to Kafka to get the data stream
  val rawEventsStream = env
    .addSource(KafkaFactory.createMessageBusConsumer(config).asInstanceOf[KafkaStringConsumer])

  // part 1: just log it
  val clickStream = rawEventsStream.map(s => Click.fromString(s))
  // clickStream.addSink(click => LOG.info(s"CLICK -> URL: ${click.get.raw_url}"))

  // part 2: filter the customers
  val loggedInCustomersStream = clickStream
    .filter(_.isDefined)
    .map(_.get)
    .filter(_.raw_user_id.isDefined)
  // loggedInCustomersStream.addSink(click => LOG.info(s"Customer: ${click.raw_user_id.get}, URL: ${click.raw_url}"))

  // part 3: enrich data (feature extraction)
  val richStream = loggedInCustomersStream
    .map(click => click.copy(
      rich_page_type = ClickDataEnricher.getPageType(click),
      rich_product_category = ClickDataEnricher.getProductCategory(click)))

  // richStream.addSink(click => LOG.info(s"Customer: ${click.raw_user_id.get}, Page type: ${click.rich_page_type.get}, Category: ${click.rich_product_category.getOrElse("none")}"))

  // part 4: check how long customers spend on a product page before visiting the cart
  // normal pattern: home -> search -> pdp (3) -> cart

  richStream.filter(_.raw_user_id.get.endsWith("1")).addSink(click => LOG.info(s"Customer: ${click.raw_user_id.get}, Page type: ${click.rich_page_type.get}, Category: ${click.rich_product_category.getOrElse("none")}"))



  //
//  val stream = env
//    .addSource(new FlinkKafkaConsumer011[String](config.kafkaConfig.rawDataTopic, new SimpleStringSchema(), properties))
//    .map(Click.fromString(_))
//    // set event timestamp
//    .assignTimestampsAndWatermarks(new ClickTimestampAndWatermarkGenerator).name("Getting event time")
//    .filter(_.category.isDefined)
//    .map(_.category.get)
//    .map(s => Tuple2(s, 1)).name("Creating tuples")
//    // group by word
//    .keyBy(_._1)
//    // group by period
//    .timeWindow(Time.seconds(config.sparkConfig.windowDuration))
//    // count the words per period
//    .apply(new CategoryCountWindowFunction()).name("Counting categories")
//
//  stream
//    .windowAll(SlidingEventTimeWindows.of(Time.seconds(config.sparkConfig.windowDuration), Time.seconds(config.sparkConfig.slideDuration.toLong)))
//    .apply(new CategorySumWindowFunction(10)).name("Top 10 categories per window")
//    .addSink{listPerPeriod =>
//      if (listPerPeriod.nonEmpty) {
//        val dt = new DateTime(listPerPeriod.head.timeStamp)
//        val s = s"## ============ WINDOW START : ${dt.toString("dd-MM-yyyy hh:mm:sss.SSS")} =========="
//        LOG.info((s :: listPerPeriod.map(s => f"## Count of ${s.count}%5d : ${s.word}")).mkString("\n"))
//      }}.name("Printing results")

  env.execute("clickstream")
}
