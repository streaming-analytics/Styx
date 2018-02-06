package com.styx

import com.styx.common.Logging
import com.typesafe.config.Config
import com.styx.domain.kafka.TopicDef
import com.styx.frameworks.kafka.{StyxKafkaConsumer, StyxKafkaProducer}

trait KafkaConnection extends Logging {

  def config: Config

  def brokerAddress: Seq[String]

  def brokerAddressString = brokerAddress.mkString(",")

  def producerTopicDef: TopicDef

  def consumerTopicDef: TopicDef

  val consumerGroup = "defaultTestGroup"

  lazy val (kafkaProducer, kafkaConsumer): (StyxKafkaProducer, StyxKafkaConsumer) = (createProducer, createConsumer)

  def createProducer: StyxKafkaProducer = {
    logger.info(s"Created producer for topic ${producerTopicDef.kafkaTopic} with group ${consumerGroup}")
    new StyxKafkaProducer(brokerAddressString, producerTopicDef)
  }

  def createConsumer: StyxKafkaConsumer = {
    val consumer = new StyxKafkaConsumer(brokerAddressString, consumerGroup, consumerTopicDef)
    consumer.seekToEnd()
    logger.info(s"Created consumer for topic ${consumerTopicDef.kafkaTopic} with group ${consumerGroup}")
    consumer
  }
}
