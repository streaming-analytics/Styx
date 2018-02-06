package com.styx.frameworks.cassandra

import com.typesafe.config.Config
import com.styx.common.InstanceGraphiteConfig

object CassandraConnectionFactory {

  def connectToCassandra(config: Config, globalGraphite: InstanceGraphiteConfig): CassandraRepository = {
    new CassandraRepository(config)
  }
}
