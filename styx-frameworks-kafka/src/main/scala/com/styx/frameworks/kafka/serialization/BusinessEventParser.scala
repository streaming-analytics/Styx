package com.styx.frameworks.kafka.serialization

import com.styx.common.LogTryImplicit._
import com.styx.domain.events.{BaseBusinessEvent, KafkaEvent}
import org.joda.time.DateTime

import scala.util.Try

case object BusinessEventParser extends EventParser[BaseBusinessEvent] {
  val timeFieldName = "EVENT_TIME"
  val accNumField = "ACC_NUM"
  val cardIdField = "CARD_ID"
  val eventField = "EVENT"

  def map(in: KafkaEvent): Try[BaseBusinessEvent] = {
    for (timeStr <- in.extractField(timeFieldName);
         time <- Try{DateTime.parse(timeStr.toString)}
           .logFailure(e => logger.error(s"Failed to parse '$timeStr' as timestamp. Cannot assign event timestamp.", e));
         accNumStr <- in.extractField(accNumField);
         accNum <- Try{accNumStr.toString.toInt}
           .logFailure(e => logger.error(s"Failed to parse '$accNumStr' as int. Cannot assign account number.", e));
         cardIdStr <- in.extractField(cardIdField).map(_.toString);
         eventStr <- in.extractField(eventField).map(_.toString)
    ) yield {
      BaseBusinessEvent(time, accNum, cardIdStr, eventStr, in.event.payload)
    }
  }
}