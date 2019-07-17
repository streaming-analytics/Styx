package ai.styx.frameworks.kafka

import java.util.Properties
import java.util.concurrent.TimeUnit

import ai.styx.domain.events.BaseEvent
import ai.styx.frameworks.interfaces.MessageBusProducer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

class KafkaEventProducer(properties: Properties) extends MessageBusProducer {
  private val producer = new KafkaProducer[String, Map[String, AnyRef]](properties)

  override type T = BaseEvent

  def send(topic: String, message: T): Unit = {
    val record = new ProducerRecord[String, Map[String, AnyRef]](message.topic, message.payload)
    producer.send(record)
  }
}

class KafkaStringProducer(properties: Properties) extends MessageBusProducer {
  private val producer = new KafkaProducer[String, String](properties)

  override type T = String

  def send(topic: String, message: T): Unit = {
    val record = new ProducerRecord[String, String](topic, message)
    producer.send(record)
  }

  def close(timeout: Long, timeUnit: TimeUnit): Unit = producer.close(timeout, timeUnit)
}