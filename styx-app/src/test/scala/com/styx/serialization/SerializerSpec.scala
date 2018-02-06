package com.styx.serialization

import java.util.{Calendar, UUID}

import com.styx.common.{BaseSpec, ConfigUtils}
import com.styx.domain.events.BaseCcEvent
import com.styx.domain.kafka.TopicDefManager
import com.styx.frameworks.kafka.flink.KafkaSchemaFactory
import org.joda.time.DateTime

class SerializerSpec extends BaseSpec {
  "EncryptionKeyedSerializer" should "serialize CC event" in {

    val cc = BaseCcEvent(DateTime.now(), Map(
      "ACC_NUM" -> "1",
      "CARD_ID" -> "c1",
      "id" -> UUID.randomUUID().toString,
      "trace_id" -> "d59b9605af43",
      "type" -> "notification",
      "created" -> Calendar.getInstance.getTimeInMillis.toString,
      "body" -> Map(
        "customer_id" -> "1",
        "channel" -> "push.fcm.app_1",
        "template_reference" -> "ABCDEF",
        "Address" -> "dXpnmK8AZqg:APA91bEf8xzSxkQD6-zqoCS8fux--62VJ5xDRpamPSZX5IBo0PMtmlx9gmZkdvIhlU5lAsjGBX4GSmLaHwj_kZZ429YgAhvagtYWkvTL-_eo5GulC9gSO-OxVsXhDYY03vd6Y1x2b85n",
        "data" -> Map(
          "title" -> "custom Title",
          "message" -> "This is a unit test message")
      )))

    val config = ConfigUtils.loadConfig(Array[String]())
    val allTopicDefs = TopicDefManager.getKafkaTopics(config.getConfig("kafka"))
    val writeTopic = config.getConfig("post").getString(s"write.topic")
    val writeTopicDef = allTopicDefs(writeTopic)

    val ccEventToPayload: BaseCcEvent => Map[String, AnyRef] = _.payload
    val ccEventToTopic: BaseCcEvent => String = _ => writeTopicDef.kafkaTopic // "testTopic"
    val writeSchema = KafkaSchemaFactory.createKeyedSerializer[BaseCcEvent](Seq(writeTopicDef), ccEventToTopic, ccEventToPayload, _ => "")

    assert(writeSchema.serializeValue(cc).nonEmpty)
  }

}
