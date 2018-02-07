package com.styx.runner

import com.styx.common.Logging
import com.styx.domain.kafka.TopicDef
import com.styx.frameworks.kafka.{StyxKafkaConsumer, StyxKafkaProducer}
import com.typesafe.config.Config

trait KafkaConnection extends Logging {

  def config: Config

  def brokerAddress: Seq[String]

  def brokerAddressString: String = brokerAddress.mkString(",")

  def producerTopicDef: TopicDef

  def consumerTopicDef: TopicDef

  val consumerGroup = "defaultTestGroup"

  lazy val (kafkaProducer, kafkaConsumer): (StyxKafkaProducer, StyxKafkaConsumer) = (createProducer, createConsumer)

  def createProducer: StyxKafkaProducer = {
    logger.info(s"Creating producer for topic ${producerTopicDef.kafkaTopic} with group $consumerGroup")
    new StyxKafkaProducer(brokerAddressString, producerTopicDef)
  }

  def createConsumer: StyxKafkaConsumer = {
    logger.info(s"Creating consumer for topic ${consumerTopicDef.kafkaTopic} with group $consumerGroup")
    val consumer = new StyxKafkaConsumer(brokerAddressString, consumerGroup, consumerTopicDef)
    consumer.seekToEnd()
    consumer
  }
}
