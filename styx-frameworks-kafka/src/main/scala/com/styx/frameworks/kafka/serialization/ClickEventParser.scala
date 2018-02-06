package com.styx.frameworks.kafka.serialization

import com.styx.common.LogTryImplicit._
import com.styx.domain.events.{BaseClickEvent, KafkaEvent}
import org.joda.time.DateTime

import scala.util.Try

case object ClickEventParser extends EventParser[BaseClickEvent] {

  val (timestamps, eventtime, header, eventId, login) = ("TIMESTAMPS", "EVENT_TIME", "HEADER", "EVENT_ID", "LOGIN")

  def map(fromSource: KafkaEvent): Try[BaseClickEvent] = {
    for {
      headerMapAnyRef: AnyRef <- Try(fromSource.event.payload(header)).logFailure(e => logger.error(s"Compulsory field $header not found: ${e.getMessage}"))
      headerMap <- Try(headerMapAnyRef match {
        case hd: Map[String, AnyRef] => hd
        case _ => throw new Exception("Cannot cast this to a hash map")
      })
      //TODO: check if this can encapsulated -assuming the handling of the two time strings remian the same in final implementation
      time <- (fromSource extractField timestamps).map(_.toString).flatMap(dateTimeStr => Try(DateTime.parse(dateTimeStr)))
      eventtime <- (fromSource extractField eventtime).map(_.toString).flatMap(dateTimeStr => Try(DateTime.parse(dateTimeStr)))
      eventId <- Try(headerMap("EVENT_ID") toString)
      login <- Try(fromSource extractField login toString)
    } yield BaseClickEvent(eventtime, login, eventId, fromSource.event.payload)
  }.logFailure(e => logger.error("failed to turn event from given event into a ClickEvent", e))
}