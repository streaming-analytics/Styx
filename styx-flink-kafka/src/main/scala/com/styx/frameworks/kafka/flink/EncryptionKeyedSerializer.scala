package com.styx.frameworks.kafka.flink

import com.styx.common.Logging
import com.styx.domain.kafka.TopicDef
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema

import scala.reflect.ClassTag

class EncryptionKeyedSerializer[T: ClassTag](val topicDefs: Seq[TopicDef], toTopic: T=> String, toPayload: T => Map[String, AnyRef], keyExtractor: T => String) extends KeyedSerializationSchema[T] with Logging{
   // TODO: implement encrypter
  // @transient private lazy val encrypter = topicDefs.map(topicDef=>topicDef.kafkaTopic -> KafkaEncryptionFactory.getCachedEncrypter(topicDef)).toMap

  private val topicSpecs = topicDefs.map(topicDef=>topicDef.kafkaTopic->topicDef).toMap

    override def serializeValue(element: T): Array[Byte] = {
      try {
        val destTopic = toTopic(element)

        Serialization.serialize(destTopic)
        //encrypter(destTopic).encrypt((toPayload(element) + ("c3keyidentifier" -> "Huh?", "c4keyidentifier" -> "OokHuh?", "schemaversion" -> topicSpecs(destTopic).kafkaSchemaVersion)).asJava)
      }
      catch {
        case t: Throwable =>
          logger.error(s"ERROR while serializing value of type ${element.getClass.toString}. NO notification will be send out! Error message: ${t.getMessage}. Cause: ${t.getCause.getMessage}.")
          Array.empty
      }
    }

    override def serializeKey(element: T): Array[Byte] = {
      val output = keyExtractor(element).toCharArray.map(_.toByte)
      debug(s"SerializeKey invoked for $element, the keyed byte array will be ${output.toString}")
      output
    }

    override def getTargetTopic(element: T): String = {
      toTopic(element)
    }
}
