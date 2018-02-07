package com.styx.frameworks.kafka

import java.util.concurrent.Executors

import com.styx.domain.kafka.Type.Type
import com.styx.domain.kafka.{TopicDef, Type}
import com.styx.common.Logging
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

class StyxKafkaConsumer(brokerAddress: String, consumerGroup: String, topicDef: TopicDef) extends Logging {

  implicit val executionContext: ExecutionContextExecutor  = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  val consumerProperties: Map[String, Object] = Map(
    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> brokerAddress,
    ConsumerConfig.GROUP_ID_CONFIG -> consumerGroup,
    ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
    ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[ByteArrayDeserializer]
//    "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
//    "value.deserializer" -> "org.apache.kafka.common.serialization.ByteArrayDeserializer",
//    "value.serializer" -> "org.apache.kafka.common.serialization.ByteArraySerializer"
    // TODO
   // KafkaSerdesConfig.MAP_SCHEMA_NAME_CONFIG -> topicDef.messageNameInProtofile,
   // KafkaSerdesConfig.MAP_C1_SCHEMA_FILE_CONFIG -> topicDef.schemaFiles(Type.C1).fileLocation,
   // KafkaSerdesConfig.MAP_SCHEMA_VERSION_CONFIG -> topicDef.ingKafkaSchemaVersion
  )
  val consumer = new KafkaConsumer[String, java.util.Map[String, AnyRef]](consumerProperties.asJava)

  def seekToEnd(): Unit = {
    val topicPartitions = getTopicPartitions(topicDef.kafkaTopic)
    consumer.assign(topicPartitions.asJava)
    consumer.seekToEnd(topicPartitions.asJava)
  }

  def getTopicPartitions(topic: String): mutable.Buffer[TopicPartition] = {
    val partitionInfos = consumer.partitionsFor(topic).asScala
    val topicPartitions: mutable.Buffer[TopicPartition] = partitionInfos.map(partitionInfo => new TopicPartition(topic, partitionInfo.partition()))
    topicPartitions
  }

  @SuppressWarnings(Array("all"))
  def asyncReceive(consumerTimeout: Long): Future[Iterable[java.util.Map[String, AnyRef]]] = {
    val recordsFuture = Future { consumer.poll(consumerTimeout) }

    recordsFuture.map {
      records => records.records(topicDef.kafkaTopic).asScala.map(extractC1Field).map(_.get) // future should still catch exceptions from Try.get
    }
  }

  def pollRecords(consumerTimeout: Long): Iterable[java.util.Map[String, AnyRef]] = {
    val polledRecords = consumer.poll(consumerTimeout)
    polledRecords.records(topicDef.kafkaTopic).asScala.map(extractC1Field).map {
      case Success(records) => records
      case Failure(exception) => logger.error("Exception thrown when polling from Kafka", exception); new java.util.HashMap[String, AnyRef]()
    }
  }

  val extractC1Field: ConsumerRecord[String, java.util.Map[String, AnyRef]] => Try[java.util.Map[String, AnyRef]] = extractField(Type.C1)

  // classifierType: ClassifierType
  def extractField(classifierType: Type): ConsumerRecord[String, java.util.Map[String, AnyRef]] => Try[java.util.Map[String, AnyRef]] = {
    data => {
      val extractedEvent: Try[java.util.Map[String, AnyRef]] = data.value().get(classifierType) match {
        // @unchecked is fine only in test code - it could cause unexpected ClassCastException if used in real, production code
        case event: java.util.Map[String, AnyRef] @unchecked => Success(event)
        case _ => Failure(new ClassCastException("Could not extract proper Event"))
      }
      extractedEvent
    }
  }

}
