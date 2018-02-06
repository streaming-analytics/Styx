package com.styx.shopping

import com.styx.common.LogOptionImplicit._
import com.styx.common.OptConfigImplicit._
import com.styx.connectors.SourceFactory
import com.styx.domain.events.{BaseBusinessEvent, BaseTransactionEvent}
import com.styx.frameworks.flink.datagenerators.{RandomBusinessEventSourceFunction, RandomTransactionEventSourceFunction}
import com.styx.frameworks.kafka.flink.TransactionEventDeserialize
import com.styx.interfaces.repository.{CriteriaFilterRepository, CustomerProfileRepository, NotificationFilterRepository}
import com.typesafe.config.Config
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

class ConfigBasedDatagenJobBuilder(config: Config,
                                        env: StreamExecutionEnvironment,
                                        configureCustomerProfile: (Config) => () => CustomerProfileRepository,
                                        configureCriteriaFilterRepository: (Config) => () => CriteriaFilterRepository,
                                        configureNotificationRepository: (Config) => () => NotificationFilterRepository
                                       ) extends ConfigBasedShoppingJobBuilder(config,
                                                                                        env,
                                                                                        configureCustomerProfile,
                                                                                        configureCriteriaFilterRepository,
                                                                                        configureNotificationRepository) {

  def readTransactionsFromDatafile(): Option[DataStream[BaseTransactionEvent]] = {
    implicit val typeInfo: TypeInformation[BaseTransactionEvent] = TypeInformation.of(classOf[BaseTransactionEvent])

    for (genConfig <- getConfigOpts("datagen");
         sourcePath <- genConfig.optString("datafile").logNone(logger.error("Could not find datafile settings"))
    ) yield {
      SourceFactory.createEventStreamFromFile(env, sourcePath, genConfig.getString("name"), new TransactionEventDeserialize())
    }
  }

  def transactionEventsToKafka(transactionEvents: DataStream[BaseTransactionEvent]): Option[DataStreamSink[BaseTransactionEvent]] = {
    val keyExtractor: BaseTransactionEvent => String = _.cardId
    writeEventsToKafka(transactionEvents, "datagen", Some(keyExtractor))
  }

  def randomTransactions(): Option[DataStream[BaseTransactionEvent]] = {
    implicit val typeInfo: TypeInformation[BaseTransactionEvent] = TypeInformation.of(classOf[BaseTransactionEvent])

    for (
      genConfig <- getConfigOpts("datagen");
      delay <- genConfig.optInt("delay")
    ) yield {
      val num = genConfig.optInt("num")
      generateRandomEvents(new RandomTransactionEventSourceFunction(num, delay), genConfig.getString("name"))
    }
  }

  def randomBusinessEvents(): Option[DataStream[BaseBusinessEvent]] = {
    implicit val typeInfo: TypeInformation[BaseBusinessEvent] = TypeInformation.of(classOf[BaseBusinessEvent])

    for (
      genConfig <- getConfigOpts("datagen");
      delay <- genConfig.optInt("delay")
    ) yield {
      val num = genConfig.optInt("num")
      generateRandomEvents(new RandomBusinessEventSourceFunction(num, delay), genConfig.getString("name"))
    }
  }

}
