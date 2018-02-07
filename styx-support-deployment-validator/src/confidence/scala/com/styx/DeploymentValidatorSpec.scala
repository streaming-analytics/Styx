package com.styx

import com.styx.common.ConfigUtils
import com.typesafe.config.Config
import com.styx.domain.kafka.{TopicDef, TopicDefManager}
import com.styx.runner.ShoppingSpec

import scala.collection.JavaConverters._

// TODO this could also be named EndToEndSpec, or something more specific (DeployedEndToEndSpec? AcceptanceSpec?), because it will execute all scenarios from ShoppingSpec
class DeploymentValidatorSpec extends ShoppingSpec {

  val config: Config = ConfigUtils.loadConfig(
    Some("styx-appRunner/src/main/resources/reference.conf"),
    Map("styx." + configNameForDataFile -> "/raw-events.csv")
  )

  override def brokerAddress: Seq[String] = config.getStringList("cep.write.bootstrap.servers").asScala

  val rawEventTopicName: String = config.getString("cep.read.topic")
  val businessEventTopicName: String = config.getString("cep.write.topic")

  lazy val topicDefinitionMap: Map[String, TopicDef] = TopicDefManager.getKafkaTopics(config.getConfig("kafka"))

  override def producerTopicDef: TopicDef = topicDefinitionMap(rawEventTopicName)

  override def consumerTopicDef: TopicDef = topicDefinitionMap(businessEventTopicName)
}
