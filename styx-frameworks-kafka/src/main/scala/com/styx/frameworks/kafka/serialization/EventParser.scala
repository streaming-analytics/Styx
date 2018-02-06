package com.styx.frameworks.kafka.serialization

import com.styx.domain.events.KafkaEvent
import com.styx.common.Logging

import scala.util.Try

abstract class EventParser[T] extends Logging {
  def map(in: KafkaEvent): Try[T]
}
