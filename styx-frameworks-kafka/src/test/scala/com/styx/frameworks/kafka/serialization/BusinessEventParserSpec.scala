package com.styx.frameworks.kafka.serialization

import com.styx.common.BaseSpec
import com.styx.domain.events.{BaseBusinessEvent, BaseKafkaEvent}
import org.joda.time.DateTime

class BusinessEventParserSpec extends BaseSpec {

  val testPayload: Map[String, String] = Map("EVENT_TIME" -> "2017-01-01T00:00:00.012+01:00", "ACC_NUM" -> "1", "CARD_ID" -> "25", "EVENT" -> "test")
  val referenceDateTime:DateTime = DateTime.parse("2017-01-01T00:00:00.012+01:00")
  val referenceBusinessEvent = BaseBusinessEvent(referenceDateTime, 1, "25", "test", testPayload)

  val rawEventFromPayload: BaseKafkaEvent = BaseKafkaEvent("testTopic", testPayload)
  val parser: BusinessEventParser.type = BusinessEventParser

  val generatedBusinessEvent: BaseBusinessEvent = parser.map(rawEventFromPayload).get

  "BusinessEventParser" should "return a business event." in {
    generatedBusinessEvent should be(referenceBusinessEvent)
  }

  it should "have an accNum that is equal to 1." in {
    withClue(generatedBusinessEvent) {
      generatedBusinessEvent.accNum should be(1)
    }
  }

  it should "have a cardId that is equal to \"25\"." in {
    withClue(generatedBusinessEvent) {
      generatedBusinessEvent.cardId should be("25")
    }
  }

  it should "have an event that is equal to test." in {
    withClue(generatedBusinessEvent) {
      generatedBusinessEvent.event should be("test")
    }
  }

  it should "have an eventTime that is equal to the date/time january 1st 2017 at 00:00:00.012H." in {
    withClue(generatedBusinessEvent) {
      generatedBusinessEvent.eventTime should be(referenceDateTime)
    }
  }

  it should "contain the payload used for event creation." in {
    withClue(generatedBusinessEvent) {
      generatedBusinessEvent.payload should be(testPayload)
    }
  }
}
