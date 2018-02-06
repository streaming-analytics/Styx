package com.styx.setup

import com.styx.interfaces.repository.{CriteriaFilterRepository, CustomerProfileRepository, NotificationFilterRepository, RepositoryInstances}
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions}

class RepositoryFactoryAdapter(styxConfig: Config) extends Serializable {

  val repositoryFactory: RepositoryFactory = determineRepositoryFactory(styxConfig)

  def determineRepositoryFactory(styxConfig: Config): RepositoryFactory = {
    styxConfig.getString("repository.type") match {
      case "cassandra" => CassandraRepositoryFactory
      case "stub" => StubRepositoryFactory
      case unrecognizedType => throw new IllegalArgumentException(s"Could not recognize repository.type: $unrecognizedType")
    }
  }

  def createRepositoryInstancesFactoryMethod(): () => RepositoryInstances = {
    createFactoryMethod(repositoryFactory.createRepositoryInstances)
  }

  def createCustomerProfileRepositoryFactoryMethod(): () => CustomerProfileRepository = {
    createFactoryMethod(repositoryFactory.createCustomerProfileRepository)
  }

  def createCriteriaFilterRepositoryFactoryMethod(): () => CriteriaFilterRepository={
    createFactoryMethod(repositoryFactory.createCriteriaFilterRepository)
  }

  def createNotificationFilterRepositoryFactoryMethod(): () => NotificationFilterRepository = {
    createFactoryMethod(repositoryFactory.createNotificationFilterRepository)
  }

  private def createFactoryMethod[T](createMethod: Config => T): () => T = {
    // A serializable object to be included in context / scope of factory method
    val configStr = configToSerializableString(styxConfig)

    def factoryMethodToBeCalledFromWithinFlinkTasks(): T = {
      val styxConfig = ConfigFactory.parseString(configStr) // this config->string->config allows to pass connection settings to flink executors// this config->string->config allows to pass connection settings to flink executors
      createMethod(styxConfig)
    }
    factoryMethodToBeCalledFromWithinFlinkTasks
  }

  private def configToSerializableString(styxConfig: Config): String = styxConfig.root().render(ConfigRenderOptions.concise())
}
