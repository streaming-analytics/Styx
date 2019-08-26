package ai.styx.app.flink

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.{BasePatternEvent, BaseTransactionEvent}
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringConsumer}
import ai.styx.usecases.shopping.CepFunction
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.joda.time.DateTime

object StyxShoppingJob extends App with Logging {

    LOG.info("Starting Flink job...")

    implicit val info = TypeInformation.of(BasePatternEvent.getClass)

    implicit val typeInfo1: TypeInformation[String] = TypeInformation.of(classOf[String])
    implicit val typeInfo2: TypeInformation[BaseTransactionEvent] = TypeInformation.of(classOf[BaseTransactionEvent])

    implicit val config: Configuration = Configuration.load()

    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)  // configure event-time characteristics
    env.getConfig.setAutoWatermarkInterval(1000)  // generate a Watermark every second
    env.setParallelism(1)

    //val rawEventFromPayload: (String, Map[String, String]) => BaseEvent =
     // (rawDataTopic, payload) => BaseEvent(rawDataTopic, payload)

    val consumer = KafkaFactory.createMessageBusConsumer(config).asInstanceOf[KafkaStringConsumer]

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
