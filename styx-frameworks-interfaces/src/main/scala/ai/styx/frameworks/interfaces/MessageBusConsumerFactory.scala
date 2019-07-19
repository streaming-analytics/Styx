package ai.styx.frameworks.interfaces

import ai.styx.common.Configuration

trait MessageBusConsumerFactory {
  def createMessageBusConsumer(config: Configuration): MessageBusConsumer
}
