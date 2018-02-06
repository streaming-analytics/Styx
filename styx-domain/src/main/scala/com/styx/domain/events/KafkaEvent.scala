package com.styx.domain.events

object KafkaEvent {
  implicit def toKafkaEvent(event: BaseKafkaEvent): KafkaEvent =
    new KafkaEvent(event)
}

class KafkaEvent(val event: BaseKafkaEvent) extends PayloadTrait[BaseKafkaEvent] {
  def updatePayload(newPayload: Map[String, AnyRef]): BaseKafkaEvent = {
    event.copy(payload = newPayload)
  }
}
