package com.styx.setup

import com.styx.common.ConfigUtils
import com.styx.frameworks.flink.AsyncRepoGenders
import com.styx.frameworks.flink.environment.StyxEnvironmentFactory
import com.styx.interfaces.repository._
import com.styx.shopping.ConfigBasedShoppingJobBuilder
import com.typesafe.config.Config
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

case class ConfigBasedJobBuilderDefaultsFixture(configOption: Option[Config] = None,
                                                env: Option[StreamExecutionEnvironment] = None,
                                                customerProfile: Option[(Config) => () => CustomerProfileRepository] = None,
                                                criteriaFilter: Option[(Config) => () => CriteriaFilterRepository] = None,
                                                notificationFilter: Option[(Config) => () => NotificationFilterRepository] = None) {

  val resolvedConfig: Config = configOption.getOrElse(ConfigUtils.loadConfig(Array()))
  val streamEnvironment: StreamExecutionEnvironment = env.getOrElse(defaultEnv())

  def withLocalEnv(): ConfigBasedJobBuilderDefaultsFixture = this.copy(env = {
    val env = StreamExecutionEnvironment.createLocalEnvironment(4)
    StyxEnvironmentFactory.setupEnvironment(env)
    Some(env)
  })

  def withEnv(env: StreamExecutionEnvironment): ConfigBasedJobBuilderDefaultsFixture = this.copy(env = Some(env))

  def withConfig(config: Config): ConfigBasedJobBuilderDefaultsFixture = this.copy(configOption = Some(config))

  def withProfileRepo(repoFactory: => CustomerProfileRepository): ConfigBasedJobBuilderDefaultsFixture = this.copy(
    customerProfile = Some((config: Config) => () => repoFactory)

  )

  def andProfileSetup(setup: Seq[CustomerProfileRepository => Future[Boolean]]): ConfigBasedJobBuilderDefaultsFixture = {
    val repoInit = customerProfile.getOrElse((config: Config) => defaultProfileRepo())(resolvedConfig)
    this.copy(
      customerProfile = Some((resolvedConfig) => () => {
        val repo = repoInit()
        MemoryRepositorySetup.ToRepositorySetup(repo).withSetup(setup)
      }
      ))
  }

  def andNotificationFilterSetup(setup: Seq[NotificationFilterRepository => Future[Boolean]]): ConfigBasedJobBuilderDefaultsFixture = {
    val repoInit = notificationFilter.getOrElse((config: Config) => defaultNotificationRepo())(resolvedConfig)
    this.copy(
      notificationFilter = Some((resolvedConfig) => () => {
        val repo = repoInit()
        MemoryRepositorySetup.ToRepositorySetup(repo).withSetup(setup)
      }
      ))
  }

  def andCriteriaFilterSetup(setup: Seq[CriteriaFilterRepository => Future[Boolean]]): ConfigBasedJobBuilderDefaultsFixture = {
    val repoInit = criteriaFilter.getOrElse((config: Config) => defaultCriteriaRepo())(resolvedConfig)
    this.copy(
      criteriaFilter = Some((resolvedConfig) => () => {
        val repo = repoInit()
        MemoryRepositorySetup.ToRepositorySetup(repo).withSetup(setup)
      }
      ))
  }

  private def defaultEnv() = {
    // FIXME unfortunately setting parallelism to 4 causes "trigger twice with 1 extra message" test to be unstable
    // watermark and windowing needs to improve -> additional tests for watermark are required
    val env = StreamExecutionEnvironment.createLocalEnvironment(1)
    StyxEnvironmentFactory.setupEnvironment(env)
    env
  }

  private def defaultProfileRepo() = {
    () => new MemoryCustomerProfileRepository()
  }

  private def defaultNotificationRepo() = {
    () => new MemoryNotificationFilterRepository()
  }
  private def defaultCriteriaRepo() = {
    () => new MemoryCriteriaFilterRepository()
  }

  private def defaultAsyncRepoGenders() = AsyncRepoGenders(10 seconds, 100)

  def createShoppingJobBuilder(): ConfigBasedShoppingJobBuilder = {
    new ConfigBasedShoppingJobBuilder(
      resolvedConfig,
      env.getOrElse(defaultEnv()),
      customerProfile.getOrElse((config: Config) => defaultProfileRepo()),
      criteriaFilter.getOrElse((config: Config) => defaultCriteriaRepo()),
      notificationFilter.getOrElse((config: Config) => defaultNotificationRepo())
    )
  }
}
