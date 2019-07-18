package ai.styx.frameworks.kafka


import ai.styx.common.{BaseSpec, Configuration}
import ai.styx.domain.events.TestEvent
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

class KafkaProducerSpec extends BaseSpec with EmbeddedKafka {

  lazy val config: Configuration = Configuration.load()

  val parallelism = 4

  override def topicsForEmbeddedKafka: Seq[LocalKafkaTopic] = Seq(
    LocalKafkaTopic(topic = config.kafkaConfig.rawDataTopic, partitions = parallelism),
    LocalKafkaTopic(topic = config.kafkaConfig.patternTopic, partitions = parallelism)
  )

  val producer: KafkaEventProducer = KafkaProducerFactory.createEventProducer(config.kafkaProducerProperties).asInstanceOf[KafkaEventProducer]

  implicit val plSer: PayloadSerializer = new PayloadSerializer
  implicit val stringSer: StringSerializer = new org.apache.kafka.common.serialization.StringSerializer

  "Kafka producer" should "start an embedded Kafka server" in {
    LOG.info("hello")

    val event = TestEvent(config.kafkaConfig.patternTopic, now, Map("element" -> "test2"))
    val record = new ProducerRecord[String, Map[String, AnyRef]](event.topic, event.payload)

    val p = this.kafkaProducer[String, Map[String, AnyRef]](event.topic, "key1", event.payload)

    LOG.info("Sending with embedded producer...")

    p.send(record)
  }

  it should "send a domain object to the Kafka bus" in {
    val event = TestEvent(config.kafkaConfig.patternTopic, now, Map("element" -> "test2"))
    producer.send(event.topic, event)
  }
}
