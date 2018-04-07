package com.styx.shopping

import com.typesafe.config.Config
import com.styx.common.ConfigUtils

/**
  * This is a Flink job that generates random transaction events, and publishes them on the Kafka bus.
  */
object StyxDataGeneratorJob {

  def main(args: Array[String]): Unit = {
    val config: Config = ConfigUtils.loadConfig(args)

    val jobFactory = ConfigBasedJobBuilderDefaults.datagenJobWithDefaultsGivenConfig(Some(config))
    for (
      transactionEvents <- jobFactory.readTransactionsFromDatafile();
      _ <- jobFactory.transactionEventsToKafka(transactionEvents)
    ) {}
    jobFactory.execute(Some("Styx-DataGen"))
  }
}
