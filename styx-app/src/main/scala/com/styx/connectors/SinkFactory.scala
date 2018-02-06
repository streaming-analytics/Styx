package com.styx.connectors

import java.util.Properties

import com.styx.domain.events.PayloadEvent
import com.styx.domain.kafka.TopicDef
import com.styx.frameworks.kafka.flink.KafkaSchemaFactory
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer09

import scala.reflect.ClassTag

object SinkFactory {

  def createEventSink[T <: PayloadEvent : ClassTag](topicDef: TopicDef, props: Properties, keyExtractor: T => String, flushOnCheckpoint: Boolean): SinkFunction[T] = {
    val eventToPayload: T => Map[String, AnyRef] = _.payload
    val eventToProtobufMessageName: T => String = _ => topicDef.kafkaTopic
    val writeSchema = KafkaSchemaFactory.createKeyedSerializer[T](Seq(topicDef), eventToProtobufMessageName, eventToPayload, keyExtractor)
    val kafkaSink: FlinkKafkaProducer09[T] =
      new FlinkKafkaProducer09[T](topicDef.kafkaTopic, writeSchema, props)
    kafkaSink.setFlushOnCheckpoint(flushOnCheckpoint)
    kafkaSink
  }
}