package com.styx.domain.events

object CcEvent {
  implicit def toCcEvent(event: BaseCcEvent): CcEvent =
    new CcEvent(event)
}

class CcEvent(val event: BaseCcEvent) extends PayloadTrait[BaseCcEvent] {
  def updatePayload(newPayload: Map[String, AnyRef]): BaseCcEvent = {
    event.copy(payload = newPayload)
  }
}
