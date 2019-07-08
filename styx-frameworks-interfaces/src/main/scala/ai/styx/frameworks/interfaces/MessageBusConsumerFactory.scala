package ai.styx.frameworks.interfaces

import java.util.Properties

trait MessageBusConsumerFactory {
  def createMessageBusConsumer(properties: Properties): Any
}
