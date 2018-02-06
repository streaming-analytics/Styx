package com.styx.frameworks.kafka.serialization

import com.styx.common.LogTryImplicit._
import com.styx.domain.events.{BaseTransactionEvent, KafkaEvent}
import org.joda.time.DateTime

import scala.util.Try

case object TransactionEventParser extends EventParser[BaseTransactionEvent] {
  val timeFieldName = "TRS_TIME"
  val accNumField = "ACC_NUM"
  val cardIdField = "CARD_ID"
  val amountField = "TRS_AMOUNT"

  def map(in: KafkaEvent): Try[BaseTransactionEvent] = {
    logger.trace(s"Received KafkaEvent: $in")
    for (timeStr <- in.extractField(timeFieldName);
         trsTime <- Try {
           DateTime.parse(timeStr.toString)
         }
           .logFailure(e => logger.error(s"Failed to parse '$timeStr' as timestamp. Cannot assign event timestamp.", e));
         accNumStr <- in.extractField(accNumField);
         accNum <- Try {
           accNumStr.toString.toInt
         }
           .logFailure(e => logger.error(s"Failed to parse '$accNumStr' as int. Cannot assign account number.", e));
         cardIdStr <- in.extractField(cardIdField);
         amountStr <- in.extractField(amountField);
         amount <- Try {
           amountStr.toString.toDouble
         }
           .logFailure(e => logger.error(s"Failed to parse '$amountStr' as double. Cannot assign transaction amount.", e))
    ) yield {
      BaseTransactionEvent(trsTime, accNum, cardIdStr.toString, amount, trsTime, in.event.payload)
    }
  }

}