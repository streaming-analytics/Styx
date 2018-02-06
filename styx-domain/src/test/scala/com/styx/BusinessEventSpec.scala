package com.styx

import java.util.UUID

import com.styx.common.BaseSpec
import com.styx.domain.events.{BaseBusinessEvent, BaseTransactionEvent, BusinessEvent}
import org.joda.time.DateTime

class BusinessEventSpec extends BaseSpec {

  val referenceTrsTime: DateTime = DateTime.parse("2017-01-01T00:00:00.012+01:00")
  val referenceEventTime: DateTime = DateTime.parse("2017-01-02T00:00:00.012+01:00")

  //Test case 1 for payload with trace_id
  val testPayload: Map[String, AnyRef] = Map(
    "TIMESTAMPS" -> referenceTrsTime.toString(),
    "trace_id" -> UUID.randomUUID().toString)

  val testTransaction = BaseTransactionEvent(referenceEventTime, 1, "2", 5.0, referenceTrsTime, testPayload)
  val testBusinessEvent: BaseBusinessEvent = BusinessEvent.from(testTransaction, "test", 25)

  "BusinessEvent" should "have correct payload values" in {
    withClue("TRACE_ID should be equal to trace_id ") {
      testBusinessEvent.payload("TRACE_ID") should be(testPayload("trace_id"))
    }
    withClue("PARENT_ID should be equal to trace_id") {
      testBusinessEvent.payload("PARENT_ID") should be(testPayload("trace_id"))
    }
    withClue("TRACE should be equal to TIMESTAMPS") {
      testBusinessEvent.payload("TRACE") should be(testPayload("TIMESTAMPS"))
    }
  }
  it should "have an accNum equals to 1" in {
    testBusinessEvent.accNum should be(1)
  }
  it should "have a cardId equals to 2" in {
    testBusinessEvent.cardId should be("2")
  }
  it should "have a topic name as \"test\" " in {
    testBusinessEvent.event should be("test")
  }
  it should "have an eventTime equal to referenceEventTime" in {
    testBusinessEvent.eventTime should be(referenceEventTime)
  }


  //Test case 2 for payload without trace_id
  val testPayloadNoTraceId: Map[String, AnyRef] = Map("TIMESTAMPS" -> referenceTrsTime.toString())
  val testTransactionNoTraceId = BaseTransactionEvent(referenceEventTime, 1, "2", 5.0, referenceTrsTime, testPayloadNoTraceId)

  val testBusinessEventNoTraceId: BaseBusinessEvent = BusinessEvent.from(testTransactionNoTraceId, "test", 25)
  val testNoTraceID: AnyRef = testBusinessEventNoTraceId.payload("PARENT_ID")

  "PARENT_ID" should "have an empty value if there is no parent trace_id value in payload" in {
    testBusinessEventNoTraceId.payload("PARENT_ID") should be("")
  }
}