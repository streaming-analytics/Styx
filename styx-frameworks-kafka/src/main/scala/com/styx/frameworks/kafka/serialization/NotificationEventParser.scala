package com.styx.frameworks.kafka.serialization

import com.styx.common.LogTryImplicit._
import com.styx.domain.events.{BaseNotificationEvent, KafkaEvent}
import org.joda.time.DateTime

import scala.util.Try

case object NotificationEventParser extends EventParser[BaseNotificationEvent] {
  val timeFieldName = "EVENT_TIME"

  def map(in: KafkaEvent): Try[BaseNotificationEvent] = {
    for (timeStr <- in.extractField(timeFieldName);
         time <- Try{DateTime.parse(timeStr.toString)}
           .logFailure(e => logger.error(s"Failed to parse '$timeStr' as timestamp. Cannot assign event timestamp.", e))) yield {
      BaseNotificationEvent(time, in.event.payload)
    }
  }
}