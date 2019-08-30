package ai.styx.frameworks.ignite

import ai.styx.frameworks.interfaces.{DatabaseFactory, DatabaseFetcher, DatabaseWriter}

class IgniteFactory(node: String, port: Int, keyspace: String, tablePrefix: String) extends DatabaseFactory {
  override def createFetcher: DatabaseFetcher = new Fetcher(node, port, keyspace, tablePrefix)
  override def createWriter: DatabaseWriter = new Writer(node, port, keyspace, tablePrefix)
}
