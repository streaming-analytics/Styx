package com.styx.shopping

import com.typesafe.config.Config
import com.styx.common.ConfigUtils

/**
  * This is a separate ML job with a random stream of business events as its source
  */
object StyxAppKafkaLessMlJob {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigUtils.loadConfig(args)

    val jobFactory = ConfigBasedJobBuilderDefaults.datagenJobWithDefaultsGivenConfig(Some(config))
    for (rawSource <- jobFactory.randomBusinessEvents();
         scoresStream <- jobFactory.businessEventsToNotificationEvents(rawSource);
         ccStream <- jobFactory.notificationEventsToCcEvents(scoresStream);
         _ <- jobFactory.ccEventsToKafka(ccStream)
    ) {
    }
    jobFactory.execute()
  }
}
