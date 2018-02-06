package com.styx.frameworks.kafka.serialization

import com.styx.domain.events.KafkaEvent

import scala.util.Try

case object ClickRawEventParser extends EventParser[KafkaEvent] {

  def map(in: KafkaEvent): Try[KafkaEvent] = {
    Try(in)
  }
}
