package ai.styx.app.flink

import ai.styx.common.{Configuration, Logging}
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringConsumer}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object StyxFraudDetectionJob extends App with Logging {
  implicit val typeInfoString: TypeInformation[String] = TypeInformation.of(classOf[String])
  implicit val typeInfoUnit: TypeInformation[Unit] = TypeInformation.of(classOf[Unit])

  implicit val config: Configuration = Configuration.load()

  val env = StreamExecutionEnvironment.getExecutionEnvironment
  //env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime) // configure event-time characteristics
  //env.getConfig.setAutoWatermarkInterval(1000) // generate a Watermark every second
  env.setParallelism(1)

  val consumer = KafkaFactory.createMessageBusConsumer(config).asInstanceOf[KafkaStringConsumer]

  val input = env.addSource(consumer)

  val s = input.map(s => s) // LOG.info(s))

  s.addSink(x => LOG.info(x))

  env.execute("Styx")
}
