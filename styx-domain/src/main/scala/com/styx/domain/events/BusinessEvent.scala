package com.styx.domain.events

import java.util.UUID

import org.joda.time.format.DateTimeFormat

object BusinessEvent {
  private lazy val dtf = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

  def from(transaction: BaseTransactionEvent, event: String, balance: Double): BaseBusinessEvent =
    BaseBusinessEvent(transaction.eventTime, transaction.accNum, transaction.cardId, event, Map(
      "TRACE_ID" -> transaction.payload.getOrElse("trace_id", UUID.randomUUID().toString),
      "PARENT_ID" -> transaction.payload.getOrElse("trace_id", ""),
      "TRACE" -> transaction.payload.getOrElse("TIMESTAMPS", ""), //TODO change TIMESTAMPS field name in the whole project
      "EVENT_CD" -> event,
      "CARD_ID" -> transaction.cardId,
      "ACC_NUM" -> transaction.accNum.toString,
      "EVENT_DTTM" -> transaction.eventTime.toString(dtf), // the event time of the last transaction
      "BALANCE" -> balance.toString))

  def from(click: BaseClickEvent, event: String): BaseBusinessEvent =
    BaseBusinessEvent(click.eventTime, 0, "no_card", event, Map(
      "TRACE_ID" -> click.payload.getOrElse("trace_id", UUID.randomUUID().toString),
      "PARENT_ID" -> click.payload.getOrElse("trace_id", ""),
      "TRACE" -> click.payload.getOrElse("TIMESTAMPS", ""), //TODO change TIMESTAMPS field name in the whole project
      "EVENT_CD" -> event,
      "ACC_NUM" -> click.payload.getOrElse("ACC_NUM", "000")))

  implicit def toBusinessEvent(event: BaseBusinessEvent): BusinessEvent =
    new BusinessEvent(event)
}

class BusinessEvent(val event: BaseBusinessEvent) extends PayloadTrait[BaseBusinessEvent] {
  def updatePayload(newPayload: Map[String, AnyRef]): BaseBusinessEvent = {
    event.copy(payload = newPayload)
  }
}
