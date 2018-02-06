package com.styx.shopping

import com.styx.common.ConfigUtils
import com.typesafe.config.Config

/**
  * This job runs all processes in one environment, in 1 long pipeline without intermediate kafka busses (except the datagen).
  *
  * There are also independent jobs available if each one needs to run in its own environment/job
  */
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