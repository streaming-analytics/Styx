package com.styx.frameworks.kafka

import java.util.concurrent.Executors

import com.styx.domain.kafka.TopicDef
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{ProducerRecord, RecordMetadata}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class StyxKafkaProducer(brokerAddress: String, topicDef: TopicDef) {
  implicit val executionContext: ExecutionContextExecutor  = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  val kafkaProperties = Map(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokerAddress)
  val kafkaProducer = StyxKafkaProducerFactory.createProducer(topicDef, kafkaProperties)

  // TODO could be extracted to class or function common for all senders to raw event topic within the project
  val keyExtractor: java.util.Map[String, AnyRef] => String = _.get("CARD_ID").toString

  def send(event: java.util.Map[String, AnyRef]): Future[RecordMetadata] = send(event, keyExtractor)

  def send(event: java.util.Map[String, AnyRef], keyExtractor: java.util.Map[String, AnyRef] => String): Future[RecordMetadata] = {
    val key = keyExtractor(event)
    Future {
      kafkaProducer.send(new ProducerRecord[String, java.util.Map[String, AnyRef]](topicDef.kafkaTopic, key, event)).get
    }
  }

  def waitForAllSendingToComplete(): Unit = {
    kafkaProducer.flush()
  }

  def close(): Unit = {
    kafkaProducer.close()
  }
}
