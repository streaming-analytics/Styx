package com.styx.frameworks.flink.shopping

import com.styx.domain.events.BaseTransactionEvent
import com.styx.common.Logging
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow
import org.apache.flink.util.Collector

class CepWindowResultFunction extends WindowFunction[BaseTransactionEvent, TransactionWindow, String, GlobalWindow] with Logging {

  def apply(key: String, window: GlobalWindow, input: Iterable[BaseTransactionEvent], out: Collector[TransactionWindow]): Unit = {
    logger.debug(s"Collected window of ${input.size} elements for account id $key") // This can be more than the Gender nr
    out.collect(
      TransactionWindow(
        getLatestEvent(input),
        getEarliestEvent(input).eventTime,
        getTotalAmount(input)
      )
    )
  }

  def getTotalAmount(input: Iterable[BaseTransactionEvent]): Double = {
    input.map(_.amount).sum
  }

  def getEarliestEvent(input: Iterable[BaseTransactionEvent]): BaseTransactionEvent = {
    input.minBy(_.eventTime.getMillis)
  }

  def getLatestEvent(input: Iterable[BaseTransactionEvent]): BaseTransactionEvent = {
    input.maxBy(_.eventTime.getMillis)
  }
}

