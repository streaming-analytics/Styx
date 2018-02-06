package com.styx.frameworks.kafka.flink

import com.styx.domain.events.{BaseKafkaEvent, BaseTransactionEvent}
import com.styx.frameworks.kafka.serialization.TransactionEventParser

import scala.util.Try

class TransactionEventDeserialize extends EventDeserializer[BaseTransactionEvent] {
  val parser: TransactionEventParser.type = TransactionEventParser

  def map(in: BaseKafkaEvent): Try[BaseTransactionEvent] = parser.map(in)
}