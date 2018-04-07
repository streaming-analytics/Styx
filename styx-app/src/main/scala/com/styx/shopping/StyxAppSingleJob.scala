package com.styx.shopping

import com.typesafe.config.Config
import com.styx.common.ConfigUtils

/**
  * This job runs all processes in one environment, in 1 long pipeline without intermediate kafka buses.
  * The source is a Kafka stream with raw transaction events.
  *
  * There are also independent jobs available if each one needs to run in its own environment/job
  */
object StyxAppSingleJob {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigUtils.loadConfig(args)

    val jobFactory = ConfigBasedJobBuilderDefaults.shoppingJobWithDefaultsGivenConfig(Some(config))
    for (rawSource <- jobFactory.readTransactionsFromKafka();
         businessStream <- jobFactory.transactionsToBusinessEvents(rawSource);
         scoresStream <- jobFactory.businessEventsToNotificationEvents(businessStream);
         ccStream <- jobFactory.notificationEventsToCcEvents(scoresStream);
         _ <- jobFactory.ccEventsToKafka(ccStream)
    ) {}
    jobFactory.execute()
  }
}