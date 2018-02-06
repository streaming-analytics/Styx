package com.styx.frameworks.kafka.flink

import com.styx.domain.events.BaseKafkaEvent
import com.styx.frameworks.kafka.serialization.EventParser
import org.apache.flink.api.common.functions.RichMapFunction

import scala.util.Try

abstract class EventDeserializer[T] extends RichMapFunction[BaseKafkaEvent, Try[T]] {
    val parser: EventParser[T]

  def map(in: BaseKafkaEvent): Try[T]
}
