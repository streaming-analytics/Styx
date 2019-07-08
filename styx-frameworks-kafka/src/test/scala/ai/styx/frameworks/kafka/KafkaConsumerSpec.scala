package ai.styx.frameworks.kafka

import java.util.Properties
import ai.styx.common.{BaseSpec, ConfigUtils}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.StringSerializer

class KafkaConsumerSpec extends BaseSpec with EmbeddedKafka {

  val parallelism = 4

  val config: Config = ConfigFactory.load()
  val readProperties: Properties = ConfigUtils.propertiesFromConfig(config.getConfig("kafka.read"))
  val writeProperties: Properties = ConfigUtils.propertiesFromConfig(config.getConfig("kafka.write"))

  val readTopic: String = config.getString("kafka.read.topic")
  val writeTopic: String = config.getString("kafka.write.topic")

  override def topicsForEmbeddedKafka: Seq[LocalKafkaTopic] = Seq(
    LocalKafkaTopic(topic = readTopic, partitions = parallelism),
    LocalKafkaTopic(topic = writeTopic, partitions = parallelism)
  )

  //val producer: MessageBusProducer = KafkaProducerFactory.createEventProducer(writeProperties)
  val producer: KafkaStringProducer = KafkaProducerFactory.createStringProducer(writeProperties).asInstanceOf[KafkaStringProducer]

  implicit val plSer: PayloadSerializer = new PayloadSerializer
  implicit val stringSer: StringSerializer = new org.apache.kafka.common.serialization.StringSerializer

  // TODO
  // TODO the FlinkKafkaConsumer only works with a running Flink instance, so use embedded Flink.
  // TODO

  "Kafka Consumer" should "receive a message" in {
    val consumer = new KafkaConsumerFactory().createMessageBusConsumer(readProperties)

    //val event = TestEvent(writeTopic, now, Map("element" -> "test3"))

    producer.send("hello") //event)
  }
}
