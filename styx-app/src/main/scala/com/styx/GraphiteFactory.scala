package com.styx

import java.time.Duration

import com.styx.common.InstanceGraphiteConfig
import com.typesafe.config.Config
import com.styx.config.ConfigHelper

object GraphiteFactory {

  def getGraphiteInstance(styxConfig: Config): InstanceGraphiteConfig =
    createGraphiteInstanceConfig(ConfigHelper.loadGraphiteConfig(styxConfig))

  def createGraphiteInstanceConfig(graphiteConfig: Config): InstanceGraphiteConfig = {
    InstanceGraphiteConfig(
      host = graphiteConfig.getString("host"),
      port = graphiteConfig.getInt("port"),
      environment = graphiteConfig.getString("environment"),
      root = graphiteConfig.getString("root"),
      instance = Option(System.getenv("HOSTNAME")).getOrElse("UNKNOWN_HOSTNAME"),
      pollingPeriod = Duration.ofSeconds(graphiteConfig.getInt("pollingperiod"))
    )
  }
}
