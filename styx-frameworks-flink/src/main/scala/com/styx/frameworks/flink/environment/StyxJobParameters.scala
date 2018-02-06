package com.styx.frameworks.flink.environment

import java.util

import com.typesafe.config.{Config, ConfigRenderOptions}
import org.apache.flink.api.common.ExecutionConfig.GlobalJobParameters

import scala.collection.JavaConverters._
import scala.math.Ordering.String

class StyxJobParameters(styxConfig: Config) extends GlobalJobParameters {

  val jobParameters: util.Map[String, String] = configToMap(styxConfig)

  def configToMap(config: Config): util.Map[String, String] = {

    val list = config.entrySet().asScala
      .map(
        entry => (
          entry.getKey,
          entry.getValue.render(ConfigRenderOptions.concise())
          )
      ).toList

    list.sorted.foldLeft(new util.LinkedHashMap[String, String]()) {
      case (accumulatedMap, (key, value)) =>
        accumulatedMap.put(key, value)
        accumulatedMap
    }
  }

  override def toMap: util.Map[String, String] = jobParameters
}
