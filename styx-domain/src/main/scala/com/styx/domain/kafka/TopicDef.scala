package com.styx.domain.kafka

import com.styx.domain.kafka.Type.Type

case class TopicDef(kafkaTopic: String, messageNameInProtobufFile: String, kafkaSchemaVersion: String, schemaFiles: Map[Type, TopicSecret]) {
  val fullyQualifiedName: String = s"""$kafkaTopic version "$kafkaSchemaVersion""""

  def this(kafkaTopic: String, kafkaSchemaVersion: String, schemaFiles: Map[Type, TopicSecret]) = this(kafkaTopic, kafkaTopic, kafkaSchemaVersion, schemaFiles)
}

object TopicDef {
  def apply(kafkaTopic: String, kafkaSchemaVersion: String, schemaFiles: Map[Type, TopicSecret]): TopicDef = new TopicDef(kafkaTopic, kafkaSchemaVersion, schemaFiles)
}