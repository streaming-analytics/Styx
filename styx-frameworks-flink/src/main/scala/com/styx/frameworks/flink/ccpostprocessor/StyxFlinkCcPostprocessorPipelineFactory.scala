package com.styx.frameworks.flink.ccpostprocessor

import java.util.{Calendar, UUID}

import com.styx.common.Logging
import com.styx.domain.events.CcEvent._
import com.styx.domain.events.NotificationEvent._
import com.styx.domain.events.{BaseCcEvent, BaseNotificationEvent}
import org.apache.flink.streaming.api.scala._

object StyxFlinkCcPostprocessorPipelineFactory extends Logging {

  def createPostProcessorPipeline(sourceStream: DataStream[BaseNotificationEvent]): DataStream[BaseCcEvent] = {
    logger.info("Setting up Post-Processor!")
    sourceStream
      .map(event => event.addTimeStamp("POST_FIRST"))
      .map(notificationEvent => {
        val message = s"[POST-PROCESSOR] Kafka and Flink say: $notificationEvent"
        logger.debug(message)
        notificationEvent
      }).map(postProcessor _).name("PostProcessor")
      .map(event => event.addTimeStamp("POST_LAST"))
  }

  def postProcessor(ne: BaseNotificationEvent): BaseCcEvent = {
    logger.info("Received _notification_ event for customer with ACC_NUM=" + ne.payload("ACC_NUM"))

    val postProcessorTime = Calendar.getInstance().getTimeInMillis

    val customerCommunicationsEvent = BaseCcEvent(ne.eventTime, Map(
      "ACC_NUM" -> ne.payload("ACC_NUM"),
      "CARD_ID" -> ne.payload("CARD_ID"),
      "id" -> UUID.randomUUID().toString,
      "type" -> ne.payload("EVENT"),
      "created" -> postProcessorTime.toString, // new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(postProcessorTime),
      "TIMESTAMPS" -> ne.payload("TIMESTAMPS"),
      "trace_id" -> ne.payload("trace_id"),
      "MSG" -> ne.payload("MESSAGE"),
      "SCORE" -> ne.payload("SCORE"),
      "body" -> Map(
        "customer_id" -> ne.payload("ACC_NUM"),
        "channel" -> "push.fcm.app_1",
        "template_reference" -> "ABCDEF",
        "Address" -> "dXpnmK8AZqg:APA91bEf8xzSxkQD6-zqoCS8fux--62VJ5xDRpamPSZX5IBo0PMtmlx9gmZkdvIhlU5lAsjGBX4GSmLaHwj_kZZ429YgAhvagtYWkvTL-_eo5GulC9gSO-OxVsXhDYY03vd6Y1x2b85n",
        "data" -> Map(
          "title" -> "custom Title",
          "message" -> ne.payload("MESSAGE"))
      )))
    logger.info(s"Outputting new CcEvent $customerCommunicationsEvent")
    customerCommunicationsEvent
  }
}
