package com.styx.domain.events

import java.util.Calendar

import com.styx.common.LogTryImplicit._
import com.styx.common.Logging

import scala.util.Try

trait PayloadTrait[T <: PayloadEvent] extends Logging {
  def event: T

  protected def updatePayload(newPayload: Map[String, AnyRef]): T

  def addTimeStamp(name: String): T = {
    val tsReceived = Calendar.getInstance().getTimeInMillis
    addTimeStamp(name, tsReceived)
  }

  def addTimeStamp(name: String, tsReceived: Long): T = {
    updatePayload(
      event.payload + ("TIMESTAMPS" ->
        (event.payload.get("TIMESTAMPS").map(oldTimestamps => s"$oldTimestamps,").getOrElse("") + s"$name=$tsReceived"))
    )
  }

  def extractField(name: String): Try[AnyRef] = {
    Try {
      event.payload(name)
    }
      .logFailure(e => logger.error(s"Failed to extract field $name from payload", e))
  }
}
