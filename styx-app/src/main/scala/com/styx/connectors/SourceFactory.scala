package com.styx.connectors

import java.util.Properties

import com.styx.common.Logging
import com.styx.support.datagen.KafkaEventFromStringDeserialize
import com.styx.domain.events.{BaseKafkaEvent, TimedEvent}
import com.styx.domain.kafka.TopicDef
import com.styx.frameworks.flink.connectors.TimedEventWatermarkExtractor
import com.styx.frameworks.kafka.flink.{EventDeserializer, KafkaSchemaFactory}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010

import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.util.Try

object SourceFactory extends Logging {

  def toStringList(topicDefs: Seq[TopicDef]): java.util.List[String] =
    topicDefs.map(_.kafkaTopic).toList.asJava

  private def createKafkaEventSource(topicDef: Seq[TopicDef], props: Properties): SourceFunction[Try[BaseKafkaEvent]] = {
    val rawEventFromPayload: (String, Map[String, String]) => BaseKafkaEvent =
      (topic, payload) => BaseKafkaEvent(topic, payload)
    val readSchema = KafkaSchemaFactory.createKeyedDeserializer(topicDef, rawEventFromPayload)
    val kafkaSource = new FlinkKafkaConsumer010[Try[BaseKafkaEvent]](toStringList(topicDef), readSchema, props)
    kafkaSource
  }

  private def createKafkaEventStream(env: StreamExecutionEnvironment, readTopicDef: TopicDef, props: Properties, jobName: String): DataStream[BaseKafkaEvent] = {
    val rawEventSource = createKafkaEventSource(Seq(readTopicDef), props)
    val sourceStream = env.addSource(rawEventSource).name(s"Source stream of $jobName from ${readTopicDef.kafkaTopic}")
    sourceStream
      .filter(_.isSuccess).name("Select successful protobuf deserialize") // if decrypting from kafka succeeded
      .flatMap(_.toOption)
  }

  def createEventStream[T <: TimedEvent : ClassTag : TypeInformation](env: StreamExecutionEnvironment,
                                                                      readTopicDef: TopicDef,
                                                                      kafkaProperties: Properties,
                                                                      jobName: String,
                                                                      deserializer: EventDeserializer[T]): DataStream[T] = {
    val deserializerName = deserializer.getClass.getSimpleName
    createKafkaEventStream(env, readTopicDef, kafkaProperties, jobName)
      .map(deserializer).name(deserializerName + "done")
      .map {
        event => logger.info("Received event")
        event }
      .flatMap(_.toOption)
      .assignTimestampsAndWatermarks(new TimedEventWatermarkExtractor[T]()).name(deserializerName + "watermark")
  }

  def createEventStreamFromFile[T <: TimedEvent : ClassTag : TypeInformation](env: StreamExecutionEnvironment, path: String, name: String, deserializer: EventDeserializer[T]): DataStream[T] = {
    env.readTextFile(path).name(s"Source stream of $name from $path")
      .map(new KafkaEventFromStringDeserialize(path))
      .map(deserializer).name("[DESERIALIZE] Event generated from file")
      .flatMap(_.toOption)
      .assignTimestampsAndWatermarks(new TimedEventWatermarkExtractor[T]()).name("[WATERMARK] Event generated from file")
  }

  def createRandomEventStream[T <: TimedEvent : ClassTag : TypeInformation](env: StreamExecutionEnvironment, generator: SourceFunction[T], name: String): DataStream[T] = {
    env.addSource(generator)
      .name(s"Source stream of $name from ${generator.getClass}")
      .assignTimestampsAndWatermarks(new TimedEventWatermarkExtractor[T]()).name("Custom generator watermarker")
  }

}