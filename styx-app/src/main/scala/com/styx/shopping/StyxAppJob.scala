package com.styx.shopping

import com.styx.StyxJob
import com.styx.common.ConfigUtils
import com.typesafe.config.Config

/**
  * This job runs all processes in one environment. All events are stored on Kafka and then loaded again in the next step.
  *
  * There are also independent jobs available if each one needs to run in its own environment/job.
  */
object StyxAppJob extends StyxJob {

  def main(args: Array[String]): Unit = {
    val config = ConfigUtils.loadConfig(args)
    run(config)
  }

  override def build(config: Config): ConfigBasedShoppingJobBuilder = {
    val jobFactory = ConfigBasedJobBuilderDefaults.shoppingJobWithDefaultsGivenConfig(Some(config))
    for (transactions <- jobFactory.readTransactionsFromKafka();
         businessEvents <- jobFactory.transactionsToBusinessEvents(transactions);
         _ <- jobFactory.businessEventsToKafka(businessEvents);
         businessStream <- jobFactory.readBusinessEventsFromKafka();
         filteredBusinessStream <- jobFactory.businessEventsToFilteredBusinessEvents(businessStream);
         scores <- jobFactory.businessEventsToNotificationEvents(filteredBusinessStream);
         ccStream <- jobFactory.notificationEventsToCcEvents(scores);
         _ <- jobFactory.ccEventsToKafka(ccStream)
    ) {}
    jobFactory
  }
}
