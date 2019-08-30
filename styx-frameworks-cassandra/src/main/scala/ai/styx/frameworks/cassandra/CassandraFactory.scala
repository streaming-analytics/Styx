package ai.styx.frameworks.cassandra

import ai.styx.frameworks.interfaces.{DatabaseFactory, DatabaseFetcher, DatabaseWriter}

class CassandraFactory(node: String, port: Int, keyspace: String, tablePrefix: String) extends DatabaseFactory {
  //def this(config: CassandraConfiguration) = this(config.node, config.port, config.keyspace, config.tablePrefix)

  override def createFetcher: DatabaseFetcher = new Fetcher(node, port, keyspace, tablePrefix)
  override def createWriter: DatabaseWriter = new Writer(node, port, keyspace, tablePrefix)
}
