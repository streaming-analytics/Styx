package ai.styx.frameworks.kafka

import java.util.Properties

import ai.styx.common.{BaseSpec, ConfigUtils}
import ai.styx.domain.events.TestEvent
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

class KafkaProducerSpec extends BaseSpec with EmbeddedKafka {

  lazy val config: Config = ConfigFactory.load()
  val writeProperties: Properties = ConfigUtils.propertiesFromConfig(config.getConfig("kafka.write"))

  val parallelism = 4

  val readTopic: String = config.getString("kafka.read.topic")
  val writeTopic: String = config.getString("kafka.write.topic")

  override def topicsForEmbeddedKafka: Seq[LocalKafkaTopic] = Seq(
    LocalKafkaTopic(topic = readTopic, partitions = parallelism),
    LocalKafkaTopic(topic = writeTopic, partitions = parallelism)
  )

  val producer: KafkaEventProducer = KafkaProducerFactory.createEventProducer(writeProperties).asInstanceOf[KafkaEventProducer]

  implicit val plSer: PayloadSerializer = new PayloadSerializer
  implicit val stringSer: StringSerializer = new org.apache.kafka.common.serialization.StringSerializer

  "Kafka producer" should "start an embedded Kafka server" in {
    LOG.info("hello")

    val event = TestEvent(writeTopic, now, Map("element" -> "test2"))
    val record = new ProducerRecord[String, Map[String, AnyRef]](event.topic, event.payload)

    val p = this.kafkaProducer[String, Map[String, AnyRef]](writeTopic, "key1", event.payload)

    LOG.info("Sending with embedded producer...")

    p.send(record)
  }

  it should "send a domain object to the Kafka bus" in {
    val event = TestEvent(writeTopic, now, Map("element" -> "test2"))
    producer.send(writeTopic, event)
  }
}
