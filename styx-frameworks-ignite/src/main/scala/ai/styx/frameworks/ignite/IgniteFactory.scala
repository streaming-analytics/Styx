package ai.styx.frameworks.ignite

import ai.styx.frameworks.interfaces.{DatabaseFactory, DatabaseFetcher, DatabaseWriter}

class IgniteFactory(url: String) extends DatabaseFactory {
  override def createFetcher: DatabaseFetcher = new Fetcher(url)
  override def createWriter: DatabaseWriter = new Writer(url)
}
