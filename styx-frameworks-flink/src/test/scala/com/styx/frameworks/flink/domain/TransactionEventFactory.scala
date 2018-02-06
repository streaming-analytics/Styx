package com.styx.frameworks.flink.domain

import com.styx.domain.events.BaseTransactionEvent
import org.joda.time.{DateTime, Period, ReadablePeriod}

object TransactionEventFactory {

  val minimalCount = 5

  def examplePattern(accNum: Int = 1, cardId: String = "1", amount: Double = 10, firstTime: DateTime = DateTime.now(), step: ReadablePeriod = Period.millis(1), length: Int = minimalCount): Seq[BaseTransactionEvent] = {
    val t0 = (0 until length).map(_=>step).foldLeft(DateTime.now()){case (value, delta)=>value.minus(delta)}
    val firstEvent = BaseTransactionEvent(t0, accNum, cardId, amount, t0, Map.empty)
    (0 until length-1).foldLeft(Seq(firstEvent)){case (seq, delta)=>seq++Seq(seq.last.withEventTime(seq.last.eventTime.plus(step)))}
  }
}
