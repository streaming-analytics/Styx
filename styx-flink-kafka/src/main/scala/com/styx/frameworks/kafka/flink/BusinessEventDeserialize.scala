package com.styx.frameworks.kafka.flink

import com.styx.domain.events.{BaseBusinessEvent, BaseKafkaEvent}
import com.styx.frameworks.kafka.serialization.BusinessEventParser

import scala.util.Try

class BusinessEventDeserialize extends EventDeserializer[BaseBusinessEvent] {
  val parser = BusinessEventParser

  def map(in: BaseKafkaEvent): Try[BaseBusinessEvent] = parser.map(in)
}