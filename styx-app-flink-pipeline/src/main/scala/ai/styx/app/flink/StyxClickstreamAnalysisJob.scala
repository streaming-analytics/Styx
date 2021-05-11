package ai.styx.app.flink

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.{Click, ClickDataEnricher}
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringConsumer}
import ai.styx.usecases.clickstream.{ClickTimestampAndWatermarkGenerator, CustomerSessionProcessWindowFunction, PageVisitFunction}
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, createTypeInformation}
import org.apache.flink.streaming.api.windowing.assigners.{EventTimeSessionWindows, ProcessingTimeSessionWindows}
import org.apache.flink.streaming.api.windowing.time.Time

object StyxClickstreamAnalysisJob extends App with Logging {
  implicit val config: Configuration = Configuration.load()

  // set up Flink
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

  // connect to Kafka to get the data stream
  val rawEventsStream = env
    .addSource(KafkaFactory.createMessageBusConsumer(config).asInstanceOf[KafkaStringConsumer])

  // part 1: just log it
  val clickStream = rawEventsStream
    .map(s => Click.fromString(s))
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

  // part 4: state -> check how long customers spend on a product page before visiting the cart
  // normal pattern: home -> search -> pdp (3) -> cart
  // demonstrate that _without_ event time, cart visits can occur before page views
  // The keyed state interfaces provides access to different types of state that are all scoped to the key of the current input element.
  // --> e.g. the previous page that was visited per customer.
  val keyedStream = richStream
    .keyBy(click => click.raw_user_id.get)

  // val checkPrevious = keyedStream
    // .process(new PageVisitFunction())
    // .addSink(s => LOG.info(s"Cart visit, customer ${s._1.raw_user_id.get} was at ${s._3} ms on product page"))

  // part 5: windows -> show that with event time, order is preserved correctly
  // session window with a maximum gap of 800 ms
  // count the number of products customer has seen before viewing the cart
  // processing time: order could be wrong, so 0 products is possible
  // event time: order is correct, so 3 products are always visible

  val windowedKeyedStream = richStream
    .assignTimestampsAndWatermarks(new ClickTimestampAndWatermarkGenerator())
    .keyBy(click => click.raw_ip)
    // .window(ProcessingTimeSessionWindows.withGap(Time.milliseconds(800)))  // this will lead to incorrect counts
    .window(EventTimeSessionWindows.withGap(Time.milliseconds(800)))
    .allowedLateness(Time.milliseconds(100))
    .process(new CustomerSessionProcessWindowFunction())
    .addSink(s => LOG.info(s))

  env.execute("clickstream")
}
