package com.styx.frameworks.kafka.serialization

import com.styx.domain.events.{BaseClickEvent, BaseKafkaEvent}
import org.scalatest.FunSuite

import scala.util.Try

class ClickEventParserSpec extends FunSuite {
    
    val (eventId, traceId, sourceSystem, login, tag, sessionId, timestamps, eventtime) =
    ("testClickEventId", "testClickEventTraceId", "cep.hostName", "marpaw0770", "pncl",
      "6EH6j63UmaENKWYxRH4RxqqL5F5B8BEFTeWvadifj1zN", "2017-01-01T00:00:00.012+01:00", "2017-01-01T00:00:00.012+01:00")

  val testKafkaPayload: Map[String, AnyRef] =
    Map("HEADER" -> Map("EVENT_ID" -> eventId,
      "TRACE_ID" -> traceId,
      "SOURCE_SYSTEM" -> sourceSystem),
      "LOGIN" -> login,
      "TAG" -> tag,
      "SESSION_ID" -> sessionId,
      "TIMESTAMPS" -> timestamps,
      "EVENT_TIME" -> eventtime)

  val testKafkaEvent: BaseKafkaEvent = BaseKafkaEvent("testTopic", testKafkaPayload)

  val generatedClickEvent: Try[BaseClickEvent] = ClickEventParser.map(testKafkaEvent)

  test("parsing should be successful") {
    assert(generatedClickEvent.isSuccess)
  }

  test("the click event should have correct login info") {
    assert(generatedClickEvent.get.login.contains(login))
  }
  
  test("the click event should have correct eventId info") {
    assert(generatedClickEvent.get.eventId.contains(eventId))
  }
  
    test("the click event should have correct timestamps info") {
    assert(generatedClickEvent.get.payload("TIMESTAMPS") === timestamps)
  }
  
    test("the click event should have correct eventtime info") {
    assert(generatedClickEvent.get.payload("EVENT_TIME") === eventtime)
  }

  
  
}