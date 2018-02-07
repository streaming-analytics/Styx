package com.styx.shopping

import com.styx.StyxJob
import com.typesafe.config.Config
import com.styx.common.ConfigUtils
import com.styx.setup.AbstractConfigBasedJobBuilder

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