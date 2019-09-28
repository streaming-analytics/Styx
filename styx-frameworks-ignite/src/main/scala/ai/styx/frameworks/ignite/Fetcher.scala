package ai.styx.frameworks.ignite

import java.sql.{DriverManager, ResultSet}

import ai.styx.common.StringHelper
import ai.styx.frameworks.interfaces.DatabaseFetcher

import scala.collection.mutable.ListBuffer

class Fetcher(url: String) extends DatabaseFetcher {
  override def getItemCount(tableName: String): Long = ???

  override def getItem(id: String, tableName: String): Option[Map[String, AnyRef]] = {
    val conn = DriverManager.getConnection(url) // create new connection, ensure multi-threading
    val sql = conn.createStatement()

    val query: String = s"SELECT * FROM $tableName WHERE id='$id'"

    LOG.debug(query)
    LOG.info(s"Getting item with id $id from table $tableName...")

    try {
      val rs = sql.executeQuery(query)

      var item: Option[Map[String, AnyRef]] = None
      if (rs.next()) {
        item = Some(getMap(rs))
      }
      if (rs.next()) {
        LOG.warn(s"More than one record in table $tableName with id $id; getting the first one...")
      }

      // TODO: move to finally
      sql.close()
      conn.close()

      item
    } catch {
      case t: Throwable =>
        LOG.error(s"Unable to get item from $tableName with id $id: ${t.getMessage}", t)
        None
    }
  }

  override def getItems(tableName: String): Option[List[Map[String, AnyRef]]] = {
    val query = s"SELECT * FROM $tableName;"

    getItems(query)
  }

  override def getItems(column: String, filter: String, tableName: String): Option[List[Map[String, AnyRef]]] = {
    val conn = DriverManager.getConnection(url) // create new connection, ensure multi-threading

    val maybeInt = StringHelper.toInt(filter)
    val query = if (maybeInt.isDefined)
      s"SELECT * FROM $tableName WHERE $column=${maybeInt.get};"
    else
      s"SELECT * FROM $tableName WHERE $column='$filter';"

    LOG.info(s"Getting item where $column=$filter from table $tableName...")

    getItems(query)
  }

  override def getItems(column: String, from: String, to: String, tableName: String): Option[List[Map[String, AnyRef]]] = {
    val query = s"SELECT * FROM $tableName WHERE $column BETWEEN '$from' AND '$to';"

    LOG.info(s"Getting items where $column is between $from and $to from table $tableName...")

    getItems(query)
  }

  private def getMap(rs: ResultSet): Map[String, AnyRef] = {
    val rsmd = rs.getMetaData
    var map = scala.collection.mutable.Map[String, AnyRef]()

    for (i <- 1 to rsmd.getColumnCount) {
      // val t = rsmd.getColumnType(i) // might be useful
      map += (rsmd.getColumnName(i) -> rs.getObject(i))
    }

    map.toMap
  }

  private def getItems(query: String): Option[List[Map[String, AnyRef]]] = {
    LOG.debug(query)

    try {
      val conn = DriverManager.getConnection(url)
      val sql = conn.createStatement()

      val rs = sql.executeQuery(query)

      val list = ListBuffer[Map[String, AnyRef]]()

      while (rs.next()) {
        list.append(getMap(rs))
      }

      // TODO: move to finally
      sql.close()
      conn.close()

      Some(list.toList)
    } catch {
      case t: Throwable =>
        LOG.error(s"Unable to get items: ${t.getMessage}", t)
        None
    }
  }
}
