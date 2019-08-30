package ai.styx.frameworks.ignite

import ai.styx.domain.DomainEntity
import ai.styx.domain.utils.Column
import ai.styx.frameworks.interfaces.DatabaseWriter

class Writer(node: String, port: Int, keyspace: String, tablePrefix: String) extends DatabaseWriter {
  override def clearTable(tableName: String, indexColumns: Option[List[Column]], columns: Option[List[Column]]): Unit = ???

  override def deleteTable(tableName: String): Unit = ???

  override def createTable(tableName: String, indexColumns: Option[List[Column]], columns: Option[List[Column]]): Unit = ???

  override def putItem(id: String, tableName: String, fieldMap: Map[Column, AnyRef]): Option[String] = ???

  override def putBatchItems(tableName: String, items: Array[Map[Column, AnyRef]]): Option[String] = ???

  override def deleteItem(id: String, tableName: String): Option[String] = ???

  override def putDomainEntity[T <: DomainEntity](tableName: String, entity: T): Option[String] = ???
}
