package ai.styx.frameworks.interfaces

trait MessageBusProducer {
  type T <: Any

 def send(message: T): Unit
}
