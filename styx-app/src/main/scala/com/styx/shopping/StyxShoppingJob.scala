package com.styx.shopping

import com.typesafe.config.Config
import com.styx.common.ConfigUtils

object StyxShoppingJob {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigUtils.loadConfig(args)

    val jobFactory = ConfigBasedJobBuilderDefaults.shoppingJobWithDefaultsGivenConfig(Some(config))
    for (
      businessEvents <- jobFactory.readBusinessEventsFromKafka();
      scores <- jobFactory.businessEventsToNotificationEvents(businessEvents);
      _ <- jobFactory.notificationEventsToKafka(scores)
    ) {}
    jobFactory.execute()
  }
}