package com.styx.frameworks.kafka.flink

import com.styx.common.BaseSpec
import com.styx.domain.events.{BaseKafkaEvent, BaseTransactionEvent}
import org.joda.time.DateTime

class TransactionEventDeserializeSpec extends BaseSpec {

  val testPayload: Map[String, String] = Map("EVENT_TIME"->"2017-01-01T00:00:00.012+01:00", "ACC_NUM" -> "1", "CARD_ID" -> "25", "TRS_AMOUNT" -> "53.65", "TRS_TIME" -> "2017-01-01T12:12:12.012+01:00")
  val referenceTrsTime:DateTime = DateTime.parse("2017-01-01T12:12:12.012+01:00")
  //Parser returns trsTime as the eventTime.
  val referenceDateTime:DateTime = referenceTrsTime
  val referenceTransactionEvent = BaseTransactionEvent(referenceDateTime, 1, "25", 53.65, referenceTrsTime, testPayload)

  val testKafkaEvent: BaseKafkaEvent = BaseKafkaEvent("testTopic", testPayload)

  val generatedTransactionEvent: BaseTransactionEvent = new TransactionEventDeserialize().map(testKafkaEvent).get

  "TransactionEventDeserialize" should "return a transaction event." in {
    generatedTransactionEvent should be(referenceTransactionEvent)
  }

  it should "have an accNum that is equal to 1." in {
    withClue(generatedTransactionEvent) {
      generatedTransactionEvent.accNum should be(1)
    }
  }

  it should "have a cardId that is equal to \"25\"." in {
    withClue(generatedTransactionEvent) {
      generatedTransactionEvent.cardId should be("25")
    }
  }

  it should "have an amount that is equal to 53.65." in {
    withClue(generatedTransactionEvent) {
      generatedTransactionEvent.amount should be(53.65)
    }
  }

  it should "have a trsTime that is equal to test." in {
    withClue(generatedTransactionEvent) {
      generatedTransactionEvent.trsTime should be(referenceTrsTime)
    }
  }

  it should "have an eventTime that is equal to the date/time january 1st 2017 at 00:00:00.012H." in {
    withClue(generatedTransactionEvent) {
      generatedTransactionEvent.eventTime should be(referenceDateTime)
    }
  }

  it should "contain the payload used for event creation." in {
    withClue(generatedTransactionEvent) {
      generatedTransactionEvent.payload should be(testPayload)
    }
  }
}
