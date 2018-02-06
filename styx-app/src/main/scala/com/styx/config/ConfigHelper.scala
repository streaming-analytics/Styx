package com.styx.config

import com.typesafe.config.Config

object ConfigHelper {
  def loadGraphiteConfig(config: Config): Config =
    config.getConfig("graphite")

  def loadRepositoryConfig(config: Config): Config = {
    config.getConfig("repository")
  }
}
