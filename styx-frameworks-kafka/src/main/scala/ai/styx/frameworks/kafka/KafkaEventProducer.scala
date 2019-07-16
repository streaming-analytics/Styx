package ai.styx.frameworks.kafka

import java.util.Properties

import ai.styx.domain.events.BaseEvent
import ai.styx.frameworks.interfaces.MessageBusProducer
import org.apache.kafka.clients.producer.ProducerRecord

class KafkaEventProducer(properties: Properties)
  extends org.apache.kafka.clients.producer.KafkaProducer[String, Map[String, AnyRef]](properties) with MessageBusProducer {

  override type T = BaseEvent

  def send(message: BaseEvent): Unit = {
    val record = new ProducerRecord[String, Map[String, AnyRef]](message.topic, message.payload)
    send(record)
  }
}

class KafkaStringProducer(properties: Properties)
  extends org.apache.kafka.clients.producer.KafkaProducer[String, String](properties) with MessageBusProducer {

  val topic = properties.getProperty("topic")
  override type T = String

  def send(message: String): Unit = {
    val record = new ProducerRecord[String, String](topic, message)
    send(record)
  }
}