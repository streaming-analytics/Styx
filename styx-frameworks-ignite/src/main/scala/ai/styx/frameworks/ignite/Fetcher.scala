package ai.styx.frameworks.ignite

import ai.styx.frameworks.interfaces.DatabaseFetcher

class Fetcher(url: String) extends DatabaseFetcher {
  override def getItemCount(tableName: String): Long = ???

  override def getItem(id: String, tableName: String): Option[Map[String, AnyRef]] = ???

  override def getItems(tableName: String): Option[List[Map[String, AnyRef]]] = ???

  override def getItems(column: String, filter: String, tableName: String): Option[List[Map[String, AnyRef]]] = ???
}
