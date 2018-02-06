package com.styx.shopping

import com.styx.StyxJob
import com.typesafe.config.Config
import com.styx.common.ConfigUtils

object StyxCepJob extends StyxJob {

  def main(args: Array[String]): Unit = {
    val config = ConfigUtils.loadConfig(args)
    run(config)
  }

  override def build(config: Config): ConfigBasedShoppingJobBuilder = {
    val jobFactory = ConfigBasedJobBuilderDefaults.shoppingJobWithDefaultsGivenConfig(Some(config))
    for (transactions <- jobFactory.readTransactionsFromKafka();
         businessEvents <- jobFactory.transactionsToBusinessEvents(transactions);
         businessEventsFilterStream <- jobFactory.businessEventsToFilteredBusinessEvents(businessEvents);
         _ <- jobFactory.businessEventsToKafka(businessEventsFilterStream)
    ){}
    jobFactory
  }
}
