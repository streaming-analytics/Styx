package ai.styx.frameworks.cassandra

import com.typesafe.config.Config

object CassandraConnectionFactory {

  def connectToCassandra(config: Config): CassandraRepository = {
    new CassandraRepository(config)
  }
}
