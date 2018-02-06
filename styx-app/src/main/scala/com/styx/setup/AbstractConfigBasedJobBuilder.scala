package com.styx.setup

import com.styx.common.OptConfigImplicit._
import com.styx.common.LogOptionImplicit._
import com.styx.common.Logging
import com.typesafe.config.Config
import com.styx.common.ConfigUtils
import com.styx.connectors.{SinkFactory, SourceFactory}
import com.styx.domain.events.{PayloadEvent, TimedEvent}
import com.styx.domain.kafka.{TopicDef, TopicDefManager}
import com.styx.frameworks.flink.AsyncRepoGenders
import com.styx.frameworks.flink.datagenerators.RandomEventSourceFunction
import com.styx.frameworks.flink.shopping.FeatureToggles
import com.styx.frameworks.kafka.flink.EventDeserializer
import org.apache.flink.api.common.JobExecutionResult
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

import scala.concurrent.duration._
import scala.reflect.ClassTag

abstract class AbstractConfigBasedJobBuilder(config: Config,
                                             env: StreamExecutionEnvironment) extends Logging {

  lazy val allTopicDefs: Map[String, TopicDef] =
    TopicDefManager.getKafkaTopics(config.getConfig("kafka"))

  def asyncRepoGenders: AsyncRepoGenders = {
    AsyncRepoGenders(
      config.getInt("repository.asyncclient.timeout") seconds,
      config.getInt("repository.asyncclient.num")
    )
  }

  def featureToggles: FeatureToggles = {
    val poisonPill = config.optBoolean("featureflags.poisonpill").getOrElse(false)
    FeatureToggles(poisonPill = poisonPill)
  }

  def getConfigOpts(jobName: String): Option[Config] = {
    config.optConfig(jobName).logNone(logger.warn("No '%s' module found in configuration".format(jobName)))
  }

  private def createEventStream[T <: TimedEvent : ClassTag : TypeInformation](config: Config,
                                                 env: StreamExecutionEnvironment,
                                                 readTopicDef: TopicDef,
                                                 deserializer: EventDeserializer[T]): DataStream[T] = {
    val jobName = config.getString("name")
    val readTopic = ConfigUtils.propertiesFromConfig(config.getConfig("read"))
    SourceFactory.createEventStream(env, readTopicDef, readTopic, jobName, deserializer)
  }

  def readEventsFromKafka[T <: TimedEvent : ClassTag : TypeInformation](configJobType: String,
                             deserializer: EventDeserializer[T]): Option[DataStream[T]] = {
    for (jobConfig <- getConfigOpts(configJobType);
         readTopic <- jobConfig.optString("read.topic").logNone(logger.error("Could not find read.topic setting"));
         readTopicDef <- allTopicDefs.get(readTopic).logNone(
           logger.error(s"Could not find settings (protobuf, version, ...) in application.conf for topic $readTopic")
         )
    ) yield {
      createEventStream(jobConfig, env, readTopicDef, deserializer)
    }
  }

  def writeEventsToKafka[T <: PayloadEvent : ClassTag](events: DataStream[T],
                                            jobType: String,
                                            optKeyExtractor: Option[T => String] = None):  Option[DataStreamSink[T]] = {
    for (config <- getConfigOpts(jobType);
         writeTopic <- config.optString("write.topic");
         writeTopicDef <- allTopicDefs.get(writeTopic).logNone(
           logger.error(s"Could not find settings (protobuf, version, ...) in application.conf for topic $writeTopic")
         )
    ) yield {
      val flushOnCheckpoint = config.optBoolean("write.flush-on-checkpoint").getOrElse(true)
      def defaultKeyExtractor: T => String = _ => ""
      val keyExtractor = optKeyExtractor.getOrElse(defaultKeyExtractor)
      createEventSink(config, writeTopicDef, events, flushOnCheckpoint, keyExtractor)
    }
  }

  private def createEventSink[T <: PayloadEvent : ClassTag](config: Config,
                                                 writeTopicDef: TopicDef,
                                                 resultStream: DataStream[T],
                                                 flushOnCheckpoint: Boolean,
                                                 keyExtractor: T => String): DataStreamSink[T] = {
    val kafkaProperties = ConfigUtils.propertiesFromConfig(config.getConfig("write"))
    val sink = SinkFactory.createEventSink(writeTopicDef, kafkaProperties, keyExtractor, flushOnCheckpoint)
    resultStream.addSink(sink).name(s"Sink of ${config.getString("name")} to ${writeTopicDef.kafkaTopic}")
  }

  def generateRandomEvents[T <: TimedEvent : ClassTag : TypeInformation](randomEventSourceFunction: RandomEventSourceFunction[T], generatorJobName: String): DataStream[T] = {
    SourceFactory.createRandomEventStream(env, randomEventSourceFunction, generatorJobName)
  }

  def execute(name: Option[String] = None): JobExecutionResult = {
    logger.info("Triggering STYX job")
    env.execute(name.getOrElse("Styx"))
  }

}
