package com.styx.runner

import net.manub.embeddedkafka.{EmbeddedKafka => LocalKafka}
import org.scalatest.{BeforeAndAfterAll, Suite}

case class LocalKafkaTopic(topic: String,
                           topicConfig: Map[String, String] = Map.empty,
                           partitions: Int = 1,
                           replicationFactor: Int = 1)

trait StyxEmbeddedKafka extends BeforeAndAfterAll with LocalKafka {
  this: Suite =>

  def topicsForEmbeddedKafka: Seq[LocalKafkaTopic]

  override def beforeAll(): Unit = {
    super.beforeAll()
    LocalKafka.start()
    createTopics()
    ensureKafkaIsReady()
  }

  def ensureKafkaIsReady(): Unit = {
    val checkTopic: String = "test-kafka-startup"
    LocalKafka.publishStringMessageToKafka(checkTopic, "checkKafkaTopic")
    LocalKafka.consumeFirstStringMessageFrom(checkTopic)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    LocalKafka.stop()
  }

  def createTopics(): Unit = {
    topicsForEmbeddedKafka.foreach { localKafkaTopic =>
      LocalKafka.createCustomTopic(
        topic = localKafkaTopic.topic,
        topicConfig = localKafkaTopic.topicConfig,
        partitions = localKafkaTopic.partitions,
        replicationFactor = localKafkaTopic.replicationFactor)
    }
  }
}
