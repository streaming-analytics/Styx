package ai.styx.frameworks.interfaces

import java.util.Properties

import ai.styx.common.Configuration

trait MessageBusFactory {
  def createMessageBusConsumer(config: Configuration): MessageBusConsumer
  def createEventProducer(properties: Properties): MessageBusProducer
  def createStringProducer(properties: Properties): MessageBusProducer

}
