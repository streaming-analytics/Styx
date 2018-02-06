package com.styx.frameworks.kafka.flink

import com.styx.domain.kafka.TopicDef
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.util.serialization.{DeserializationSchema, KeyedDeserializationSchema, KeyedSerializationSchema}

import scala.reflect.ClassTag
import scala.util.Try

// important: this import is needed to access the 'createTypeInformation' macro function
import org.apache.flink.streaming.api.scala._

object KafkaSchemaFactory {

  def createKeyedDeserializer[T: TypeInformation](topicDef: Seq[TopicDef], typeConstructor: (String, Map[String, String]) => T): KeyedDeserializationSchema[Try[T]] = {
   // new DecryptionKeyedDeserializer[T](topicDef, typeConstructor)

   // implicit val typeInfo: TypeInformation[T] = TypeInformation.of(classOf[T])

    new KeyedDeserializationSchema[Try[T]] {
      override def isEndOfStream(nextElement: Try[T]): Boolean = true // ???

      override def deserialize(messageKey: Array[Byte], message: Array[Byte], topic: String, partition: Int, offset: Long): Try[T] =
        Try(Serialization.deserialize(message).asInstanceOf[T]) //???

      override def getProducedType: TypeInformation[Try[T]] = createTypeInformation[Try[T]]   // TypeInformation[T] // ???
    }
  }

  def createDeserializer[T: TypeInformation](topicDef: TopicDef, typeConstructor: Map[String, String] => T): DeserializationSchema[Try[T]] = {
    // example typeConstructor = message => message("payload")
    //new DecryptionDeserializer(topicDef, typeConstructor)

    //implicit val typeInfo: TypeInformation[T] = TypeInformation.of(classOf[T])

    new DeserializationSchema[Try[T]] {
      override def isEndOfStream(nextElement: Try[T]): Boolean = true //???

      override def deserialize(message: Array[Byte]): Try[T] = Try(Serialization.deserialize(message).asInstanceOf[T]) // ???

      override def getProducedType: TypeInformation[Try[T]] = createTypeInformation[Try[T]] // TypeInformation[T] // ???
    }
  }

  def createKeyedSerializer[T: ClassTag](topicDefs: Seq[TopicDef], toTopic: T=>String, toPayload: T => Map[String, AnyRef], keyExtractor: T => String): KeyedSerializationSchema[T] = {
    //new EncryptionKeyedSerializer[T](topicDefs, toTopic, toPayload, keyExtractor)
    new KeyedSerializationSchema[T] {
      override def serializeKey(element: T): Array[Byte] = Serialization.serialize(element) // ???

      override def getTargetTopic(element: T): String = "topic1" // TODO ???

      override def serializeValue(element: T): Array[Byte] = Serialization.serialize(element) // ???
    }
  }
}
