package ai.styx.frameworks.kafka

import java.util.Properties

import ai.styx.frameworks.interfaces.{MessageBusProducer, MessageBusProducerFactory}

object KafkaProducerFactory extends MessageBusProducerFactory {
  override def createEventProducer(properties: Properties): MessageBusProducer =
    new KafkaEventProducer(properties)

  override def createStringProducer(properties: Properties): MessageBusProducer =
    new KafkaStringProducer(properties)
}
