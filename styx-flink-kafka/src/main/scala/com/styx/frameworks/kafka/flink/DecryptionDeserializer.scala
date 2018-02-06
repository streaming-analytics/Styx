package com.styx.frameworks.kafka.flink

// TODO
//class DecryptionDeserializer[A: ClassTag](topicDef: TopicDef, typeConstructor: Map[String, String] => A) extends InfiniteStreamDeserializer[Try[A]] with Logging {
 // @transient val decrypter = KafkaEncryptionFactory.getCachedDecrypter(topicDef)
//
//  override def getProducedType: TypeInformation[Try[A]] = TypeExtractor.getForClass(implicitly[ClassTag[A]].runtimeClass.asInstanceOf[Class[Try[A]]])
//
//  override def deserialize(message: Array[Byte]): Try[A] = {
//    val eventAttributes = Try {
//      message.toMap[String, String]
//      // TODO
//      //decrypter.decrypt(message).asScala.toMap
//    }.logFailure(e => logger.error("Failed to decrypt message.", e))
//
//    eventAttributes.map(typeConstructor)
//  }
//}
