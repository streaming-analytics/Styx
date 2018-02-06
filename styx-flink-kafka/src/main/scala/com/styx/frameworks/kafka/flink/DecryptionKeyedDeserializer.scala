package com.styx.frameworks.kafka.flink

import com.styx.common.Logging
import com.styx.domain.kafka.TopicDef
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.java.typeutils.TypeExtractor

import com.styx.common.LogTryImplicit._
import scala.reflect.ClassTag
import scala.util.Try

class DecryptionKeyedDeserializer[A: ClassTag](val topicDefs: Seq[TopicDef], typeConstructor: (String, Map[String, String]) => A) extends InfiniteStreamKeyedDeserializer[Try[A]] with Logging {
  // TODO: create decrypter
  // @transient lazy val decrypter = topicDefs.map(topicDef => topicDef.ingKafkaTopic -> KafkaEncryptionFactory.getCachedDecrypter(topicDef)).toMap

  override def getProducedType: TypeInformation[Try[A]] = TypeExtractor.getForClass(implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[Try[A]]])

  override def deserialize(key: Array[Byte], message: Array[Byte], topic: String, partition: Int, offset: Long): Try[A] = {
    // we don't use the key write now, perhaps it could be used in the future.
    val eventAttributes = Try {
      Serialization.deserialize(message).asInstanceOf[Map[String,String]]

      //decrypter(topic).decrypt(message).asScala.toMap
    }.logFailure(e => logger.error("Failed to decrypt message.", e))
    eventAttributes.map(typeConstructor(topic, _))
  }
}
