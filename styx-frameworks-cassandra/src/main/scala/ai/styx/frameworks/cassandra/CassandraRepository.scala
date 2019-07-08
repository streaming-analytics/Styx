package ai.styx.frameworks.cassandra

import com.datastax.driver.core.{Cluster, Session}
import com.typesafe.config.Config

class CassandraRepository(config: Config) {
  // TODO: read contact point from config
  //val cp = config.getString("styx.repository.hosts")

  val cluster: Cluster = Cluster.builder().addContactPoint("127.0.0.1").withCredentials("username", "password").build()
  val session: Session = cluster.connect("keyspace1")
}
