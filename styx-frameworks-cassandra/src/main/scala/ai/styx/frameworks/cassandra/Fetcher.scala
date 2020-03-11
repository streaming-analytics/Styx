package ai.styx.frameworks.cassandra

import ai.styx.frameworks.interfaces.DatabaseFetcher
import com.datastax.oss.driver.api.core.cql.Row
// import com.datastax.driver.core.Row
import org.joda.time.DateTime

import scala.collection.mutable.ListBuffer

class Fetcher(node: String, port: Int, keyspace: String, tablePrefix: String) extends DatabaseFetcher {
  val connector = new CassandraConnector()
  connector.connect(node, Some(port), keyspace)
  val session = connector.getSession

  override def getItemCount(tableName: String): Long = {
    try {
      val tableNameWithPrefix = s"${tablePrefix}_$tableName"

      // TODO: BG: this query generates a server warning: Aggregation query used without partition key. To be fixed.
      val query = s"SELECT COUNT(*) FROM $tableNameWithPrefix;"
      val result = session.execute(query)

      val rows = result.all()

      rows.size() match {
        case 0 => 0
        case 1 => rows.get(0).getLong("count")
        case _ =>
          LOG.warn(s"Counting records in $tableNameWithPrefix didn't work, more than 1 row recoverd. Getting the first one...")
          rows.get(0).getLong("count")
      }
    }
    catch {
      case t: Throwable =>
        LOG.error(s"Unable to get item count from ${tablePrefix}_$tableName: ${t.getMessage}", t)
        0
    }
  }

  override def getItem(id: String, tableName: String): Option[Map[String, AnyRef]] = {
    try {
      val tableNameWithPrefix = s"${tablePrefix}_$tableName"

      val query = s"SELECT * FROM $tableNameWithPrefix WHERE id = '$id';"
      LOG.debug(query)
      val result = session.execute(query)

      val rows = result.all()

      rows.size() match {
        case 0 => None
        case 1 => Some(_createFieldMapFrom(rows.get(0)))
        case _ =>
          LOG.warn(s"More than one record in table $tableNameWithPrefix with id $id; getting the first one...")
          Some(_createFieldMapFrom(rows.get(0)))
      }
    }
    catch {
      case t: Throwable =>
        LOG.error(s"Unable to get item from ${tablePrefix}_$tableName with id $id: ${t.getMessage}", t)
        None
    }
  }

  override def getItems(tableName: String): Option[List[Map[String, AnyRef]]] = {
    try {
      val tableNameWithPrefix = s"${tablePrefix}_$tableName"

      val query = s"SELECT * FROM $tableNameWithPrefix;"
      val result = session.execute(query)

      val rows = result.all().iterator()

      val items: ListBuffer[Map[String, AnyRef]] = scala.collection.mutable.ListBuffer[Map[String, AnyRef]]()
      while (rows.hasNext) {
        items.append(_createFieldMapFrom(rows.next()))
      }

      Some(items.toList)
    }
    catch {
      case t: Throwable =>
        LOG.error(s"Unable to get all itema from ${tablePrefix}_$tableName: ${t.getMessage}", t)
        None
    }
  }

  override def getItems(column: String, filter: String, tableName: String): Option[List[Map[String, AnyRef]]] = {
    try {
      val tableNameWithPrefix = s"${tablePrefix}_$tableName"

      val query = s"SELECT * FROM $tableNameWithPrefix WHERE $column = '$filter';"
      val result = session.execute(query)

      val rows = result.all().iterator()

      val items: ListBuffer[Map[String, AnyRef]] = scala.collection.mutable.ListBuffer[Map[String, AnyRef]]()
      while (rows.hasNext) {
        items.append(_createFieldMapFrom(rows.next()))
      }

      Some(items.toList)
    }
    catch {
      case t: Throwable =>
        LOG.error(s"Unable to get all items from ${tablePrefix}_$tableName: ${t.getMessage}", t)
        None
    }
  }

  override def getItems(column: String, from: String, to: String, tableName: String): Option[List[Map[String, AnyRef]]] = {
    try {
      val tableNameWithPrefix = s"${tablePrefix}_$tableName"

      val query = s"SELECT * FROM $tableNameWithPrefix WHERE $column BETWEEN '$from' AND '$to';"
      val result = session.execute(query)

      val rows = result.all().iterator()

      val items: ListBuffer[Map[String, AnyRef]] = scala.collection.mutable.ListBuffer[Map[String, AnyRef]]()
      while (rows.hasNext) {
        items.append(_createFieldMapFrom(rows.next()))
      }

      Some(items.toList)
    }
    catch {
      case t: Throwable =>
        LOG.error(s"Unable to get all items from ${tablePrefix}_$tableName: ${t.getMessage}", t)
        None
    }
  }

  private def _createFieldMapFrom(row: Row): Map[String, AnyRef] = {
    var map = Map[String, AnyRef]()

    row.getColumnDefinitions.forEach {
      cd =>
        val columnName = cd.getName.toString
        val columnType = cd.getType.toString.toUpperCase() // TODO: check

        columnType.toUpperCase match {
          case "TEXT" => map = map + (columnName -> row.getString(columnName))
          case "VARCHAR" => map = map + (columnName -> row.getString(columnName))
          case "INT" => map = map + (columnName -> Int.box(row.getInt(columnName)))
          case "BOOLEAN" => map = map + (columnName -> Boolean.box(row.getBoolean(columnName)))
          case "TIMESTAMP" => map = map + (columnName -> new DateTime(row.getString(columnName))) // TODO: convert to timestamp
          case "DOUBLE" => map = map + (columnName -> Double.box(row.getDouble(columnName)))
          case x => LOG.warn("Unknown column type: " + columnType)
        }
    }

    map
  }
}