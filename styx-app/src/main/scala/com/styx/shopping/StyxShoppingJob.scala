package com.styx.shopping

import com.styx.StyxJob
import com.typesafe.config.Config
import com.styx.common.ConfigUtils
import com.styx.setup.AbstractConfigBasedJobBuilder

/**
  * This is a single ML job that reads business events from Kafka, perfors the ML function on them, and publishes the
  * resulting intermediate events on a Kafka topic.
  */
object StyxShoppingJob extends StyxJob {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigUtils.loadConfig(args)
    run(config)
  }

  override def build(config: Config): AbstractConfigBasedJobBuilder = {
    val jobFactory = ConfigBasedJobBuilderDefaults.shoppingJobWithDefaultsGivenConfig(Some(config))
    for (
      businessEvents <- jobFactory.readBusinessEventsFromKafka();
      scores <- jobFactory.businessEventsToNotificationEvents(businessEvents);
      _ <- jobFactory.notificationEventsToKafka(scores)
    ) {}
    jobFactory
  }
}