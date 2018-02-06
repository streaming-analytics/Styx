package com.styx.domain.events

import java.util.UUID

import com.styx.domain._

object NotificationEvent {

  def from(be: BaseBusinessEvent, n: NotificationFilter, score: Double, balance: Double, event: String): BaseNotificationEvent =
    events.BaseNotificationEvent(be.eventTime, Map(
      "EVENT_TIME" -> be.eventTime.toString,
      "ACC_NUM" -> be.accNum.toString,
      "CARD_ID" -> be.cardId,
      "NOTIFICATION" -> n,
      "EVENT" -> event,
      "SCORE" -> score.toString,
      "MESSAGE" -> n.Message
        .replace("[customer.BALANCE]", balance.toString)
        .replace("[event.EVENT]", be.event.toLowerCase)
        .replace("[notification.AMOUNT]", Math.max(5, 250 - balance).toString),
      "TIMESTAMPS" -> be.payload("TIMESTAMPS"),
      "trace_id" -> be.payload.getOrElse("trace_id", UUID.randomUUID().toString)))

  implicit def toNotificationEvent(event: BaseNotificationEvent): NotificationEvent =
    new NotificationEvent(event)
}

class NotificationEvent(val event: BaseNotificationEvent) extends PayloadTrait[BaseNotificationEvent] {

  def updatePayload(newPayload: Map[String, AnyRef]): BaseNotificationEvent = {
    event.copy(payload = newPayload)
  }
}
