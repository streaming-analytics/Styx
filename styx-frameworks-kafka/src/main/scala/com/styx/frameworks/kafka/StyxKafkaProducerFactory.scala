package com.styx.frameworks.kafka

import java.util.Properties

import com.styx.domain.kafka.TopicDef
import org.apache.kafka.clients.producer.KafkaProducer

import scala.collection.JavaConverters._

object StyxKafkaProducerFactory {

  def createProducer(topicDef: TopicDef, providedProperties: Map[String, AnyRef] = Map()): KafkaProducer[String, java.util.Map[String, AnyRef]] = {
    val kafkaProperties = providedProperties
    new KafkaProducer[String, java.util.Map[String, AnyRef]](kafkaProperties.asJava)
  }

  def createProducer(topicDef: TopicDef, kafkaProps: Properties): KafkaProducer[String, java.util.Map[String, AnyRef]] = {
    createProducer(topicDef, Map(kafkaProps.asScala.toList: _*))
  }

  def getProducer(topicDef: TopicDef, kafkaProps: Properties, clientId: String): KafkaProducer[String, String] = {
    new KafkaProducer[String, String](kafkaProps)
  }
}
