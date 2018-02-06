package com.styx.shopping

import com.styx.common.{ConfigUtils, Logging}
import com.styx.frameworks.flink.environment.StyxEnvironmentFactory
import com.styx.interfaces.repository.{CriteriaFilterRepository, CustomerProfileRepository, NotificationFilterRepository}
import com.styx.setup.RepositoryFactoryAdapter
import com.typesafe.config.Config
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object ConfigBasedJobBuilderDefaults extends Logging {

  def shoppingJobWithDefaultsGivenConfig(configOption: Option[Config] = None,
                                         env: Option[StreamExecutionEnvironment] = None,
                                         configureCustomerProfile: Option[(Config) => () => CustomerProfileRepository] = None,
                                         configureCriteriaFilterRepository: Option[(Config) => () => CriteriaFilterRepository] = None,
                                         configureNotificationRepository: Option[(Config) => () => NotificationFilterRepository] = None
                             ): ConfigBasedShoppingJobBuilder = {
    val config: Config = configOption.getOrElse(ConfigUtils.loadConfig(Array()))
    val repositoryFactory = new RepositoryFactoryAdapter(config)
    new ConfigBasedShoppingJobBuilder(
      config,
      env.getOrElse(StyxEnvironmentFactory.createEnvironment(config)),
      configureCustomerProfile.getOrElse((styxConfig: Config) => repositoryFactory.createCustomerProfileRepositoryFactoryMethod()),
      configureCriteriaFilterRepository.getOrElse((styxConfig: Config) => repositoryFactory.createCriteriaFilterRepositoryFactoryMethod()),
      configureNotificationRepository.getOrElse((styxConfig: Config) => repositoryFactory.createNotificationFilterRepositoryFactoryMethod())
    )
  }

  def datagenJobWithDefaultsGivenConfig(configOption: Option[Config] = None,
                                        env: Option[StreamExecutionEnvironment] = None,
                                        configureCustomerProfile: Option[(Config) => () => CustomerProfileRepository] = None,
                                        configureCriteriaFilterRepository: Option[(Config) => () => CriteriaFilterRepository] = None,
                                        configureNotificationRepository: Option[(Config) => () => NotificationFilterRepository] = None): ConfigBasedDatagenJobBuilder = {
    val config: Config = configOption.getOrElse(ConfigUtils.loadConfig(Array()))
    val repositoryFactory = new RepositoryFactoryAdapter(config)
    new ConfigBasedDatagenJobBuilder(
      config,
      env.getOrElse(StyxEnvironmentFactory.createEnvironment(config)),
      configureCustomerProfile.getOrElse((styxConfig: Config) => repositoryFactory.createCustomerProfileRepositoryFactoryMethod()),
      configureCriteriaFilterRepository.getOrElse((styxConfig: Config) => repositoryFactory.createCriteriaFilterRepositoryFactoryMethod()),
      configureNotificationRepository.getOrElse((styxConfig: Config) => repositoryFactory.createNotificationFilterRepositoryFactoryMethod())
    )
  }

}