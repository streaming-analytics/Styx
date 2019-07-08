package ai.styx.app

import ai.styx.common.{ConfigUtils, Logging}
import ai.styx.domain.events.{BasePatternEvent, BaseRawEvent, BaseTransactionEvent}
import ai.styx.frameworks.kafka.KafkaConsumerFactory
import ai.styx.usecases.shopping.CepFunction
import com.typesafe.config.ConfigFactory
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.joda.time.DateTime
import ai.styx.domain.events.{BasePatternEvent, BaseTransactionEvent}
import ai.styx.domain.events.BaseTransactionEvent
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala._

object StyxShoppingJob extends App with Logging {

    LOG.info("Starting Flink job...")

    implicit val info = TypeInformation.of(BasePatternEvent.getClass)

    val config = ConfigFactory.load()
    val readProperties = ConfigUtils.propertiesFromConfig(config.getConfig("kafka"))

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)  // configure event-time characteristics
    env.getConfig.setAutoWatermarkInterval(1000)  // generate a Watermark every second
    env.setParallelism(1)

    //val rawEventFromPayload: (String, Map[String, String]) => BaseEvent =
     // (topic, payload) => BaseEvent(topic, payload)

    val consumer = new KafkaConsumerFactory().createMessageBusConsumer(readProperties)

    val input = env.addSource(consumer)

    val cep = new CepFunction

    val transactions = input
      .map(s => {
        LOG.info("Received message: " + s)
        //BaseRawEvent("???", DateTime.now, Map("key1" -> "value1") )
        BaseTransactionEvent("topic1", DateTime.now.toString("yyyyMMdd:HHmmSS"), 1, "", 0, Map("key" -> "value"))
      })

    val businessEvents = cep.map(transactions)

    businessEvents.addSink(b => LOG.info(b.event))

    env.execute("Test 1")

}
