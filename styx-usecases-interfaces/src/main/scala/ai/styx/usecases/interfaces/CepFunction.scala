package ai.styx.usecases.interfaces

import ai.styx.domain.events.{BasePatternEvent, BaseRawEvent}

trait CepFunction[T <: BaseRawEvent] {
  def createBusinessEvent(rawEvent: T): BasePatternEvent
}
