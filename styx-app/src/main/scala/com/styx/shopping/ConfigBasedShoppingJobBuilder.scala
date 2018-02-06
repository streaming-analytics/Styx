package com.styx.shopping

import com.styx.domain.events._
import com.styx.frameworks.flink.AsyncRepoGenders
import com.styx.frameworks.flink.ccpostprocessor.StyxFlinkCcPostprocessorPipelineFactory
import com.styx.frameworks.flink.shopping.{FeatureToggles, ShoppingFlinkPipelineFactory}
import com.styx.frameworks.kafka.flink.{BusinessEventDeserialize, NotificationEventDeserialize, TransactionEventDeserialize}
import com.styx.interfaces.repository.{CriteriaFilterRepository, CustomerProfileRepository, NotificationFilterRepository}
import com.styx.setup.AbstractConfigBasedJobBuilder
import com.typesafe.config.Config
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.datastream.DataStreamSink
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

class ConfigBasedShoppingJobBuilder(config: Config,
                                    env: StreamExecutionEnvironment,
                                    configureCustomerProfile: (Config) => () => CustomerProfileRepository,
                                    configureCriteriaFilterRepository: (Config) => () => CriteriaFilterRepository,
                                    configureNotificationRepository: (Config) => () => NotificationFilterRepository) extends AbstractConfigBasedJobBuilder(config, env) {

  val CEP_JOB_CONFIG_NAME: String = "cep"
  val ML_JOB_CONFIG_NAME: String = "ml"
  val POST_JOB_CONFIG_NAME: String = "post"

  lazy val pipelineBuilder = PipelineBuilder(asyncRepoGenders,
    featureToggles,
    configureCustomerProfile(config),
    configureCriteriaFilterRepository(config),
    configureNotificationRepository(config)
  )

  def readTransactionsFromKafka(): Option[DataStream[BaseTransactionEvent]] = {
    implicit val typeInfo: TypeInformation[BaseTransactionEvent] = TypeInformation.of(classOf[BaseTransactionEvent])

    readEventsFromKafka(CEP_JOB_CONFIG_NAME, new TransactionEventDeserialize())
  }

  def readBusinessEventsFromKafka(): Option[DataStream[BaseBusinessEvent]] = {
    implicit val typeInfo: TypeInformation[BaseBusinessEvent] = TypeInformation.of(classOf[BaseBusinessEvent])

    readEventsFromKafka(ML_JOB_CONFIG_NAME, new BusinessEventDeserialize())
  }

  def readNotificationEventsFromKafka(): Option[DataStream[BaseNotificationEvent]] = {
    implicit val typeInfo: TypeInformation[BaseNotificationEvent] = TypeInformation.of(classOf[BaseNotificationEvent])

    readEventsFromKafka(POST_JOB_CONFIG_NAME, new NotificationEventDeserialize())
  }

  def transactionsToBusinessEvents(sourceStream: DataStream[BaseTransactionEvent]): Option[DataStream[BaseBusinessEvent]] = {
    pipelineBuilder.transactionsToBusinessEvents(sourceStream)
  }

  def businessEventsToFilteredBusinessEvents(businessEvents: DataStream[BaseBusinessEvent]): Option[DataStream[BaseBusinessEvent]] = {
    pipelineBuilder.businessEventsToFilteredBusinessEvents(businessEvents)
  }

  def businessEventsToNotificationEvents(businessEvents: DataStream[BaseBusinessEvent]): Option[DataStream[BaseNotificationEvent]] = {
    pipelineBuilder.businessEventsToNotificationEvents(businessEvents)
  }

  def notificationEventsToCcEvents(scoresStream: DataStream[BaseNotificationEvent]): Option[DataStream[BaseCcEvent]] = {
    pipelineBuilder.notificationEventsToCcEvents(scoresStream)
  }

  def businessEventsToKafka(businessEvents: DataStream[BaseBusinessEvent]): Option[DataStreamSink[BaseBusinessEvent]] = {
    val keyExtractor: BaseBusinessEvent => String = _.cardId
    writeEventsToKafka(businessEvents, CEP_JOB_CONFIG_NAME, Some(keyExtractor))
  }

  def notificationEventsToKafka(notificationEvents: DataStream[BaseNotificationEvent]): Option[DataStreamSink[BaseNotificationEvent]] = {
    writeEventsToKafka(notificationEvents, ML_JOB_CONFIG_NAME)
  }

  def ccEventsToKafka(ccEvents: DataStream[BaseCcEvent]): Option[DataStreamSink[BaseCcEvent]] = {
    writeEventsToKafka(ccEvents, POST_JOB_CONFIG_NAME)
  }

  case class PipelineBuilder(pipelineAsyncRepoGenders: AsyncRepoGenders,
                             pipelineFeatureToggles: FeatureToggles,
                             configureCustomerProfile: () => CustomerProfileRepository,
                             configureCriteriaRepository: () => CriteriaFilterRepository,
                             configureNotificationRepository: () => NotificationFilterRepository) {

    def transactionsToBusinessEvents(sourceStream: DataStream[BaseTransactionEvent]): Option[DataStream[BaseBusinessEvent]] = {
      val customerRepoFactory = configureCustomerProfile
      Some(ShoppingFlinkPipelineFactory.createCepPipeline(sourceStream, customerRepoFactory(), pipelineFeatureToggles, pipelineAsyncRepoGenders))
    }

    def businessEventsToFilteredBusinessEvents(businessEvents: DataStream[BaseBusinessEvent]): Option[DataStream[BaseBusinessEvent]] = {
      val customerRepoFactory = configureCustomerProfile
      val criteriaRepoFactory = configureCriteriaRepository
      Some(ShoppingFlinkPipelineFactory.createCepFilterPipeline(businessEvents, customerRepoFactory(), criteriaRepoFactory(), pipelineAsyncRepoGenders))
    }

    def businessEventsToNotificationEvents(businessEvents: DataStream[BaseBusinessEvent]): Option[DataStream[BaseNotificationEvent]] = {
      val customerRepoFactory = configureCustomerProfile
      val notificationRepoFactory = configureNotificationRepository
      Some(ShoppingFlinkPipelineFactory.createPipeline(businessEvents, customerRepoFactory(), notificationRepoFactory(), pipelineAsyncRepoGenders))
    }

    def notificationEventsToCcEvents(scoresStream: DataStream[BaseNotificationEvent]): Option[DataStream[BaseCcEvent]] = {
      Some(StyxFlinkCcPostprocessorPipelineFactory.createPostProcessorPipeline(scoresStream))
    }
  }

}