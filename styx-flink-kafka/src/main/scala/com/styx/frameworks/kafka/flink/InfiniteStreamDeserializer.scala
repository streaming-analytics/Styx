package com.styx.frameworks.kafka.flink

import org.apache.flink.streaming.util.serialization.DeserializationSchema

trait InfiniteStreamDeserializer[A] extends DeserializationSchema[A] with Serializable {
  override def isEndOfStream(nextElement: A): Boolean = false
}
