package com.styx.frameworks.kafka.flink

import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema

trait InfiniteStreamKeyedDeserializer[A] extends KeyedDeserializationSchema[A] with Serializable {
  override def isEndOfStream(nextElement: A): Boolean = false
}
