package com.styx.frameworks.kafka.flink

import com.styx.domain.events.{BaseKafkaEvent, BaseNotificationEvent}
import com.styx.frameworks.kafka.serialization.NotificationEventParser

import scala.util.Try

class NotificationEventDeserialize extends EventDeserializer[BaseNotificationEvent] {
  val parser: NotificationEventParser.type = NotificationEventParser

  def map(in: BaseKafkaEvent): Try[BaseNotificationEvent] = parser.map(in)
}