package ai.styx.frameworks.interfaces

import ai.styx.common.Logging

trait DatabaseFetcher extends Logging {
  def getItemCount(tableName: String): Long

  def getItem(id: String, tableName: String): Option[Map[String, AnyRef]]

  def getItems(tableName: String): Option[List[Map[String, AnyRef]]]
  def getItems(column: String, filter: String, tableName: String): Option[List[Map[String, AnyRef]]]
}
