package com.styx.shopping

import com.styx.common.ConfigUtils
import com.typesafe.config.Config

object StyxAppKafkaLessCepJob {

  def main(args: Array[String]): Unit = {

    val config: Config = ConfigUtils.loadConfig(args)

    val jobFactory = ConfigBasedJobBuilderDefaults.datagenJobWithDefaultsGivenConfig(Some(config))
    for (rawSource <- jobFactory.randomTransactions();
         businessEvents <- jobFactory.transactionsToBusinessEvents(rawSource);
         _ <- jobFactory.businessEventsToKafka(businessEvents)
    ) {
    }
    jobFactory.execute()
  }
}