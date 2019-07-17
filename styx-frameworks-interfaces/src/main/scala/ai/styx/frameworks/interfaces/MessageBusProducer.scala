package ai.styx.frameworks.interfaces

trait MessageBusProducer {
  type T <: Any

 def send(topic: String, message: T): Unit
}
