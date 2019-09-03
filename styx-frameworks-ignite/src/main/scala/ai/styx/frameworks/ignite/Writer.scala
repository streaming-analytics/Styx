package ai.styx.frameworks.ignite

import java.sql.DriverManager

import ai.styx.domain.DomainEntity
import ai.styx.domain.utils.{Column, ColumnType}
import ai.styx.frameworks.interfaces.DatabaseWriter

class Writer(url: String) extends DatabaseWriter {
  Class.forName("org.apache.ignite.IgniteJdbcThinDriver")
  private val conn = DriverManager.getConnection(url)

  override def clearTable(tableName: String, indexColumns: Option[List[Column]], columns: Option[List[Column]]): Unit = ???

  override def deleteTable(tableName: String): Unit = ???

  override def createTable(tableName: String, indexColumns: Option[List[Column]], columns: Option[List[Column]]): Unit = {
    val sql = conn.createStatement()

    var allColumns = List[Column]()

    // make sure the timestamp is included
    if (columns.isEmpty) {
      allColumns = List(Column("db_timestamp", ColumnType.TIMESTAMP))
    } else if (!columns.get.exists(_.name == "db_timestamp")) {
      allColumns = Column("db_timestamp", ColumnType.TIMESTAMP) :: columns.get
    } else {
      allColumns = columns.get
    }

    val c = if (allColumns.isEmpty) "" else
      ", " + allColumns.filter(_.name != "id").map(c => f"${c.name} ${c.`type`}").mkString(", ")

    val query: String = s"CREATE TABLE $tableName (id TEXT PRIMARY KEY $c)"

    LOG.info(s"Creating table $tableName...")
    sql.executeUpdate(query)

    //  " id INTEGER PRIMARY KEY, name VARCHAR, isEmployed tinyint(1)) WITH \"template=replicated\"")"

    if (indexColumns.isDefined) {
      indexColumns.get.foreach {
        ic =>
          val indexName = s"idx_${tableName.toLowerCase()}_${ic.name.toLowerCase()}"
          sql.executeUpdate(s"CREATE INDEX $indexName ON $tableName (${ic.name})")
          LOG.info(s"Created index $indexName")
      }
    }
  }

  override def putItem(id: String, tableName: String, fieldMap: Map[Column, AnyRef]): Option[String] = ???

  override def putBatchItems(tableName: String, items: Array[Map[Column, AnyRef]]): Option[String] = ???

  override def deleteItem(id: String, tableName: String): Option[String] = ???

  override def putDomainEntity[T <: DomainEntity](tableName: String, entity: T): Option[String] = ???
}
