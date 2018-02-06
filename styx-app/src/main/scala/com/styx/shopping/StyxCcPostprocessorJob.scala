package com.styx.shopping

import com.typesafe.config.Config
import com.styx.common.ConfigUtils

object StyxCcPostprocessorJob {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigUtils.loadConfig(args)

    val jobFactory = ConfigBasedJobBuilderDefaults.shoppingJobWithDefaultsGivenConfig(Some(config))
    for (
      scores <- jobFactory.readNotificationEventsFromKafka();
      ccStream <- jobFactory.notificationEventsToCcEvents(scores);
      _ <- jobFactory.ccEventsToKafka(ccStream)
    ) {}
    jobFactory.execute()
  }
}
