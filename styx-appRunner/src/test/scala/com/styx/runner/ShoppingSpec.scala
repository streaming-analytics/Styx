package com.styx.runner

import java.util
import java.util.concurrent.Executors

import com.styx.common.Logging
import com.styx.support.datagen.RawEventGenerator
import com.typesafe.config.Config
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor}

trait ShoppingSpec extends FlatSpecLike with Matchers with Logging with KafkaConnection {
  implicit val executionContext: ExecutionContextExecutor  = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  def config: Config

  val configNameForDataFile: String = "datagen.datafile"
  val formatter: DateTimeFormatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.getDefault)

  // secure timeout values for the slow build environment. If on local then it could be set to lower values e.g. 10 s
  val consumerTimeout = 30000L // in millis

  it should "trigger a business event on fifth transaction event within 75 minutes" in {
    sendEventsFromFileToKafka()
    val generatedBusinessEvents = kafkaConsumer.pollRecords(consumerTimeout)
    assertOneBusinessEventReceived(generatedBusinessEvents)
  }

  it should "trigger twice when six transaction events are sent" in {

  }

  it should "not trigger a business event when time difference larger than 75 minutes" in {

  }

  it should "not trigger a business event for events for another customer" in {

  }

  it should "show how system behaves when the event timestamps are unordered to document how watermark works in our implementation" in {

  }

  def assertOneBusinessEventReceived(receivedEvents: Iterable[util.Map[String, AnyRef]]): Unit = {
    assert(receivedEvents.size == 1)

    val businessEvent = receivedEvents.head
    logger.info(s"Received business event: $businessEvent")
    verifyBusinessEventFormat(businessEvent)
    verifyNoAdditionalEvents()
  }

  def sendEventsFromFileToKafka(): Unit = {
    val filename: String = config.getString(configNameForDataFile)
    for (event <- new RawEventGenerator(None, filename).createData().toList) yield kafkaProducer.send(event)
    kafkaProducer.waitForAllSendingToComplete()
  }

  def verifyBusinessEventFormat(businessEvent: java.util.Map[String, AnyRef]): Unit = {
    withClue("TRACE_ID") { businessEvent.get("TRACE_ID") should not be null }
    withClue("PARENT_ID") { businessEvent.get("PARENT_ID") should not be null }
    withClue("TRACE") { businessEvent.get("TRACE") should not be null }
    withClue("EVENT_CD") { businessEvent.get("EVENT_CD") shouldEqual "Shopping" }
    withClue("CARD_ID") { businessEvent.get("CARD_ID") shouldEqual "4037337" }
    withClue("ACC_NUM") { businessEvent.get("ACC_NUM") shouldEqual "4727314" }
    withClue("BALANCE") { businessEvent.get("BALANCE").toString.toDouble should be < 200.0 }
    val eventDttm: DateTime = parseDate(businessEvent.get("EVENT_DTTM"))
    // changing to local time does not take into account the zone, but it's okay for now
    withClue("EVENT_DTTM") { eventDttm.toLocalTime shouldEqual new DateTime(2017, 2, 17, 0, 43, 0, 65, DateTimeZone.forOffsetHours(1)).toLocalTime }
  }

  def parseDate(dateString: Object): DateTime = formatter.parseDateTime(dateString.toString)

  def verifyNoAdditionalEvents(): Unit = {
    val consumerRecords = kafkaConsumer.asyncReceive(consumerTimeout = (200 milliseconds).toMillis)
    withClue("additional events were triggered - this could be caused by sending the same data twice"){ Await.result(consumerRecords, 300 milliseconds).size shouldBe 0}
  }
}
