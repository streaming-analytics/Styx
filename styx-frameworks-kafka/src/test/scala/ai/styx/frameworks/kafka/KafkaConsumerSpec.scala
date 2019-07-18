package ai.styx.frameworks.kafka

import java.util.Properties

import ai.styx.common.{BaseSpec, Configuration}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.StringSerializer

class KafkaConsumerSpec extends BaseSpec with EmbeddedKafka {

  val parallelism = 4

  implicit val config: Configuration = Configuration.load()
  val readTopic: String = config.kafkaConfig.rawDataTopic
  val writeTopic: String = config.kafkaConfig.patternTopic

  override def topicsForEmbeddedKafka: Seq[LocalKafkaTopic] = Seq(
    LocalKafkaTopic(topic = readTopic, partitions = parallelism),
    LocalKafkaTopic(topic = writeTopic, partitions = parallelism)
  )

  val producer: KafkaStringProducer = KafkaProducerFactory.createStringProducer(config.kafkaProducerProperties).asInstanceOf[KafkaStringProducer]

  implicit val plSer: PayloadSerializer = new PayloadSerializer
  implicit val stringSer: StringSerializer = new org.apache.kafka.common.serialization.StringSerializer

  // TODO
  // TODO the FlinkKafkaConsumer only works with a running Flink instance, so use embedded Flink.
  // TODO

  "Kafka Consumer" should "receive a message" in {
    val consumer = new KafkaConsumerFactory().createMessageBusConsumer(config.kafkaConsumerProperties)

    //val event = TestEvent(writeTopic, now, Map("element" -> "test3"))

    producer.send(writeTopic, "hello") //event)
  }
}
