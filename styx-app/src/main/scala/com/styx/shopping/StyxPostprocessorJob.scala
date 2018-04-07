package com.styx.shopping

import com.typesafe.config.Config
import com.styx.common.ConfigUtils

/**
  * This is a single PP job that picks up intermediate events from Kafka, formats them, and publishes the resulting
  * notification events on a Kafka topic.
  */
object StyxPostprocessorJob {

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
