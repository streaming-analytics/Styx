package ai.styx.frameworks.kafka

import net.manub.embeddedkafka.EmbeddedKafka
import org.scalatest.{BeforeAndAfterAll, Suite}

case class LocalKafkaTopic(topic: String,
                           topicConfig: Map[String, String] = Map.empty,
                           partitions: Int = 1,
                           replicationFactor: Int = 1)

trait LocalKafka extends BeforeAndAfterAll with EmbeddedKafka {
  this: Suite =>

  def topicsForEmbeddedKafka: Seq[LocalKafkaTopic]

  override def beforeAll(): Unit = {
    super.beforeAll()
    EmbeddedKafka.start()
    createTopics()
    ensureKafkaIsReady()
  }

  def ensureKafkaIsReady(): Unit = {
    val checkTopic: String = "test-kafka-startup"
    EmbeddedKafka.publishStringMessageToKafka(checkTopic, "checkKafkaTopic")
    EmbeddedKafka.consumeFirstStringMessageFrom(checkTopic)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    EmbeddedKafka.stop()
  }

  def createTopics(): Unit = {
    topicsForEmbeddedKafka.foreach { localKafkaTopic =>
      EmbeddedKafka.createCustomTopic(
        topic = localKafkaTopic.topic,
        topicConfig = localKafkaTopic.topicConfig,
        partitions = localKafkaTopic.partitions,
        replicationFactor = localKafkaTopic.replicationFactor)
    }
  }
}
