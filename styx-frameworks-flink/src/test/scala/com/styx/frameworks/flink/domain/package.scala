package com.styx.frameworks.flink

import com.styx.domain.events.BaseTransactionEvent
import org.joda.time.DateTime

package object domain {

  implicit class PrototypeTransactionEvent(val ev: BaseTransactionEvent) extends AnyVal {
    def withEventTime(newTime: DateTime): BaseTransactionEvent =
      ev.copy(eventTime = newTime, trsTime = newTime)
  }
}
