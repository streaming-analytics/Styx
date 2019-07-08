package ai.styx.frameworks.kafka

import ai.styx.domain.events.BaseEvent
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.util.serialization.{DeserializationSchema, KeyedDeserializationSchema, KeyedSerializationSchema}

import scala.reflect.ClassTag
import scala.util.Try

// important: this import is needed to access the 'createTypeInformation' macro function
import org.apache.flink.streaming.api.scala._

class StringSchema extends KeyedDeserializationSchema[String] with KeyedSerializationSchema[String] {
  override def isEndOfStream(nextElement: String): Boolean = false

  override def deserialize(messageKey: Array[Byte], message: Array[Byte], topic: String, partition: Int, offset: Long): String =
    Serialization.deserialize(message).asInstanceOf[String]

  override def serializeKey(element: String): Array[Byte] = Serialization.serialize(element)

  override def getTargetTopic(element: String): String = "test"  // TODO

  override def serializeValue(element: String): Array[Byte] = Serialization.serialize(element)

  override def getProducedType: TypeInformation[String] = createTypeInformation[String] // TypeExtractor.getForClass(BaseEvent.getClass)
}

class BaseEventSchema extends KeyedDeserializationSchema[BaseEvent] with KeyedSerializationSchema[BaseEvent] {
  override def isEndOfStream(nextElement: BaseEvent): Boolean = false

  override def deserialize(messageKey: Array[Byte], message: Array[Byte], topic: String, partition: Int, offset: Long): BaseEvent =
    Serialization.deserialize(message).asInstanceOf[BaseEvent]

  override def serializeKey(element: BaseEvent): Array[Byte] = Serialization.serialize(element)

  override def getTargetTopic(element: BaseEvent): String = "topic1"  // TODO

  override def serializeValue(element: BaseEvent): Array[Byte] = Serialization.serialize(element)

  override def getProducedType: TypeInformation[BaseEvent] = createTypeInformation[BaseEvent] // TypeExtractor.getForClass(BaseEvent.getClass)
}

object SerializationFactory {

  def createKeyedDeserializer[T: TypeInformation](typeConstructor: (String, Map[String, String]) => T): KeyedDeserializationSchema[Try[T]] = {
    new KeyedDeserializationSchema[Try[T]] {
      override def isEndOfStream(nextElement: Try[T]): Boolean = false

      override def deserialize(messageKey: Array[Byte], message: Array[Byte], topic: String, partition: Int, offset: Long): Try[T] =
        Try(Serialization.deserialize(message).asInstanceOf[T]) //???

      override def getProducedType: TypeInformation[Try[T]] = createTypeInformation[Try[T]]   // TypeInformation[T] // ???
    }
  }

  def createDeserializer[T: TypeInformation](typeConstructor: Map[String, String] => T): DeserializationSchema[Try[T]] = {
    // example typeConstructor = message => message("payload")
    //new DecryptionDeserializer(topicDef, typeConstructor)

    //implicit val typeInfo: TypeInformation[T] = TypeInformation.of(classOf[T])

    new DeserializationSchema[Try[T]] {
      override def isEndOfStream(nextElement: Try[T]): Boolean = false

      override def deserialize(message: Array[Byte]): Try[T] = Try(Serialization.deserialize(message).asInstanceOf[T]) // ???

      override def getProducedType: TypeInformation[Try[T]] = createTypeInformation[Try[T]] // TypeInformation[T] // ???
    }
  }

  def createKeyedSerializer[T: ClassTag](toTopic: T=>String, toPayload: T => Map[String, AnyRef], keyExtractor: T => String): KeyedSerializationSchema[T] = {
    //new EncryptionKeyedSerializer[T](topicDefs, toTopic, toPayload, keyExtractor)
    new KeyedSerializationSchema[T] {
      override def serializeKey(element: T): Array[Byte] = Serialization.serialize(element) // ???

      override def getTargetTopic(element: T): String = "topic1" // TODO ???

      override def serializeValue(element: T): Array[Byte] = Serialization.serialize(element) // ???
    }
  }
}
