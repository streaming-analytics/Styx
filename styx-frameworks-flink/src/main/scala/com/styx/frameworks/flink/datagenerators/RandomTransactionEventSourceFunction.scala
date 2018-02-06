package com.styx.frameworks.flink.datagenerators

import java.util.UUID

import com.styx.domain.events.BaseTransactionEvent
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext
import org.joda.time.DateTime

import scala.util.Random

class RandomTransactionEventSourceFunction(Gender: Option[Int] = None, delayMs: Int = 100) extends RandomEventSourceFunction[BaseTransactionEvent](Gender, delayMs) {

  @transient lazy val random = Random

  def serialVersionUID: Long = 2174904787118597072L

  var running = true
  var i = 0L
  val maxSingleTransaction = 450

  def run(ctx: SourceContext[BaseTransactionEvent]) {
    random.setSeed(0)
    // TODO, if we want to generate the actual test data from within FLINK we could call the RawEventGenerator to get actual data
    while (running) {
      i += 1
      // two customers
      // Customer: 1661819, Cardid: 2343159 ,Age: 20
      // Customer: 3945919, Cardid: 402875 ,Age: 40
      val accnum = if (i % 2 == 0) "1661819" else "2343159"
      val cardid = if (i % 2 == 0) "3945919" else "402875"

      val amount = Math.floor(100 * Math.abs((random.nextInt % maxSingleTransaction) + random.nextDouble)) / 100
      val eventTime = DateTime.now()

      ctx.collect(BaseTransactionEvent(eventTime, accnum.toInt, cardid, amount, eventTime,
        Map(
          "ACC_NUM" -> accnum,
          "CARD_ID" -> cardid,
          "TRS_TIME" -> eventTime.toString,
          "TRS_AMOUNT" -> amount.toString,
          "TIMESTAMPS" -> s"GEN=${eventTime.getMillis.toString}",
          "trace_id" -> UUID.randomUUID().toString
        )))
      Thread.sleep(delayMs)
    }
    logger.warn(s"Stopped generating raw events after $i items")
  }

  def cancel() {
    running = false
  }
}
