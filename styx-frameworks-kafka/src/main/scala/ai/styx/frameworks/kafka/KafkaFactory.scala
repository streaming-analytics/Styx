package ai.styx.frameworks.kafka

import java.util.Properties

import ai.styx.common.Configuration
import ai.styx.frameworks.interfaces.{MessageBusConsumer, MessageBusFactory, MessageBusProducer}

object KafkaFactory extends MessageBusFactory {
  override def createEventProducer(properties: Properties): MessageBusProducer =
    new KafkaEventProducer(properties)

  override def createStringProducer(properties: Properties): MessageBusProducer =
    new KafkaStringProducer(properties)

  override def createMessageBusConsumer(config: Configuration): MessageBusConsumer =
    new KafkaStringConsumer(config)

}
