package com.styx.setup

import com.styx.interfaces.repository._
import com.typesafe.config.Config
import com.styx.GraphiteFactory
import com.styx.config.ConfigHelper
import com.styx.dsl.JitCriteriaParser
import com.styx.frameworks.cassandra.{CassandraConnectionFactory, CassandraRepository}
import com.styx.frameworks.cassandra.cep.CassandraCepDefinitionsRepository
import com.styx.frameworks.cassandra.criteria.CassandraCriteriaRepository
import com.styx.frameworks.cassandra.customerprofiles.CassandraCustomerProfileRepository
import com.styx.frameworks.cassandra.models.CassandraModelRepository
import com.styx.frameworks.cassandra.notifications.CassandraNotificationRepository
import com.styx.frameworks.openscoring.PmmlModelInstanceRepository

object CassandraRepositoryFactory extends RepositoryFactory {

  override def createRepositoryInstances(styxConfig: Config): RepositoryInstances = {
    val repo: CassandraRepository = createCassandraRepository(styxConfig)
    RepositoryInstances(createCassandraCustomerProfileRepository(repo),
      new CriteriaBasedFilterRepository(new CassandraCriteriaRepository(repo), new JitCriteriaParser()),
      new ModelBasedNotificationFilterRepository(
        new PmmlModelInstanceRepository(new CassandraModelRepository(repo)),
        new CassandraNotificationRepository(repo)),
      new CassandraCepDefinitionsRepository(repo))
  }

  override def createCustomerProfileRepository(styxConfig: Config): CustomerProfileRepository = {
    val repo: CassandraRepository = createCassandraRepository(styxConfig)
    createCassandraCustomerProfileRepository(repo)
  }

  override def createNotificationFilterRepository(styxConfig: Config): NotificationFilterRepository = {
    val repo: CassandraRepository = createCassandraRepository(styxConfig)
    new ModelBasedNotificationFilterRepository(
      new PmmlModelInstanceRepository(new CassandraModelRepository(repo)),
      new CassandraNotificationRepository(repo))
  }

  override def createCriteriaFilterRepository(styxConfig: Config) : CriteriaFilterRepository = {
    val repo: CassandraRepository = createCassandraRepository(styxConfig)
    new CriteriaBasedFilterRepository(new CassandraCriteriaRepository(repo), new JitCriteriaParser())
  }

  def createCassandraRepository(styxConfig: Config): CassandraRepository = {
    val globalGraphite = GraphiteFactory.getGraphiteInstance(styxConfig)
    val repo = CassandraConnectionFactory.connectToCassandra(ConfigHelper.loadRepositoryConfig(styxConfig), globalGraphite)
    repo
  }

  private def createCassandraCustomerProfileRepository(repo: CassandraRepository): CustomerProfileRepository = {
    new CassandraCustomerProfileRepository(repo)
  }
}
