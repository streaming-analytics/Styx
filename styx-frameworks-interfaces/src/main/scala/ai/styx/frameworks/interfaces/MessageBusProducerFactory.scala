package ai.styx.frameworks.interfaces

import java.util.Properties

trait MessageBusProducerFactory {
  def createEventProducer(properties: Properties): MessageBusProducer
  def createStringProducer(properties: Properties): MessageBusProducer
}
