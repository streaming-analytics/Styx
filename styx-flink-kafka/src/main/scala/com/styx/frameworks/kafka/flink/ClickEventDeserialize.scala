package com.styx.frameworks.kafka.flink

import com.styx.domain.events.{BaseClickEvent, BaseKafkaEvent}
import com.styx.frameworks.kafka.serialization.ClickEventParser

import scala.util.Try

class ClickEventDeserialize extends EventDeserializer[BaseClickEvent] {
  val parser: ClickEventParser.type = ClickEventParser

  def map(in: BaseKafkaEvent): Try[BaseClickEvent] = parser.map(in)
}
