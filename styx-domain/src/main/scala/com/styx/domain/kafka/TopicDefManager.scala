package com.styx.domain.kafka

import com.typesafe.config.Config
import com.styx.domain.kafka.Type.Type

import scala.collection.JavaConverters._

// TODO: move to styx-kafka ?
object TopicDefManager {
  val prefix = "schemas"

  private def getTopicDirs(config: Config, kafkaSchemaRootPath: String): Map[String, TopicDef] = {
    val rootConfig = config.getConfig(prefix)
    rootConfig.root().asScala.keys.map(k => k -> rootConfig.getConfig(k))
      .flatMap { case (topic, topicConf) =>
        if (topicConf.hasPath("schemaversion")) {
          val schemaVersion: String = topicConf.getString("schemaversion")
          val topicFiles = (1 to 4)
            .map(i => i -> s"$kafkaSchemaRootPath/$topic/$topic.c$i.desc")
            .filter { case (i, file) => TopicDef.getClass.getResourceAsStream(file) != null }
            .map { case (i, file) => getTopicFile(topicConf, i, file) }
            .toMap
          Some(topic -> TopicDef(topic, schemaVersion, topicFiles))
        } else None
      }
  }.toMap

  private def getTopicFile(topicConf: Config, level: Int, filename: String): (Type, TopicSecret) = {
    val cLevel = level match {
      case 1 => Type.C1
      case 2 => Type.C2
      case 3 => Type.C3
      case 4 => Type.C4
    }
    val sharedSecretKey: String = s"C${level}sharedsecret"
    if (topicConf.hasPath(sharedSecretKey))
      cLevel -> TopicSecret(s"$filename", Some(topicConf.getString(sharedSecretKey)))
    else
      cLevel -> TopicSecret(s"$filename", None)
  }

  def getKafkaTopics(config: Config): Map[String, TopicDef] = {
    val kafkaSchemaRootPath = config.getString("schemafiles")
    getTopicDirs(config, kafkaSchemaRootPath)
  }

}
