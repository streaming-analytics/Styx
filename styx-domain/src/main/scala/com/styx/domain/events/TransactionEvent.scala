package com.styx.domain.events

/**
  * The raw event that contains a single payments transaction of a customer
  */
object TransactionEvent {
  implicit def toTransactionEvent(event: BaseTransactionEvent): TransactionEvent =
    new TransactionEvent(event)
}

class TransactionEvent(val event: BaseTransactionEvent) extends PayloadTrait[BaseTransactionEvent] {
  def updatePayload(newPayload: Map[String, AnyRef]): BaseTransactionEvent = {
    event.copy(payload = newPayload)
  }
}
