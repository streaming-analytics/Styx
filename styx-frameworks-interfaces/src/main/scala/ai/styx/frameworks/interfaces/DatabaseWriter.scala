package ai.styx.frameworks.interfaces

import java.sql.Timestamp
import java.time.format.DateTimeFormatter

import ai.styx.common.Logging
import ai.styx.domain.DomainEntity
import ai.styx.domain.utils.{Column, ColumnType}
import org.joda.time.DateTime

import scala.util.Random

trait DatabaseWriter extends Logging {
  final val TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS" // https://apacheignite-sql.readme.io/docs/data-types#section-timestamp: "yyyy-MM-dd hh:mm:ss[.nnnnnnnnn]"

  def clearTable(tableName: String, indexColumns: Option[List[Column]] = None, columns: Option[List[Column]] = None): Unit

  def deleteTable(tableName: String): Unit

  def createTable(tableName: String, indexColumns: Option[List[Column]] = None, columns: Option[List[Column]] = None): Unit

  def putItem(id: String, tableName: String, fieldMap: Map[Column, AnyRef]): Option[String]

  def putBatchItems(tableName: String, items: Array[Map[Column, AnyRef]]): Option[String]

  def deleteItem(id: String, tableName: String): Option[String]

  def putDomainEntity[T <: DomainEntity](tableName: String, entity: T): Option[String]

  def createIDFor(tableName: String, offset: Int = -1): String = {
    val timestamp = DateTime.now().getMillis
    val randomValue = Random.nextInt(10000)

    if (offset >= 0) {
      s"$tableName-$timestamp-$randomValue-$offset"
    } else {
      s"$tableName-$timestamp-$randomValue"
    }
  }

  def createColumns(fields: Map[String, AnyRef]): Map[Column, AnyRef] = {
    // supported types: text, int, double, timestamp
    fields.filter(_._1 != "id").map { field =>
      field._2 match {
        case null => Column(field._1, ColumnType.TEXT) -> "null"
        case None => Column(field._1, ColumnType.TEXT) -> "None"
        case _: String => Column(field._1, ColumnType.TEXT) -> field._2
        case _: java.lang.Integer => Column(field._1, ColumnType.INT) -> field._2
        case _: java.lang.Long => Column(field._1, ColumnType.LONG) -> field._2
        case _: java.lang.Double => Column(field._1, ColumnType.DOUBLE) -> field._2
        case _: java.lang.Boolean => Column(field._1, ColumnType.BOOLEAN) -> field._2
        case ts: Timestamp => Column(field._1, ColumnType.TIMESTAMP) -> DateTimeFormatter.ofPattern(TIMESTAMP_PATTERN).format(ts.toLocalDateTime)
        case dt: DateTime => Column(field._1, ColumnType.TIMESTAMP) -> dt.toString(TIMESTAMP_PATTERN)  // MM/dd/yyyy_HH:mm:ss:SSS  ??
        case e: Enumeration => Column(field._1, ColumnType.TEXT) -> e.toString()
        case _ => Column(field._1, ColumnType.TEXT) -> field._2.toString // other types: just translate to string
      }
    }
  }
}
