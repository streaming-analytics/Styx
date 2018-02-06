package com.styx

import com.typesafe.config.Config
import com.styx.setup.AbstractConfigBasedJobBuilder

trait StyxJob {

  def build(config: Config): AbstractConfigBasedJobBuilder

  def run(config: Config, jobName: Option[String] = None): Unit = {
    val jobFactory = build(config)
    jobFactory.execute(jobName)
  }
}
