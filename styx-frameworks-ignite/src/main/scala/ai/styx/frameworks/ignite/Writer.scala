package ai.styx.frameworks.ignite

import java.sql.{DriverManager, Timestamp}
import java.time.format.DateTimeFormatter

import ai.styx.domain.DomainEntity
import ai.styx.domain.utils.{Column, ColumnType}
import ai.styx.frameworks.interfaces.DatabaseWriter
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

class Writer(url: String) extends DatabaseWriter {
  Class.forName("org.apache.ignite.IgniteJdbcThinDriver")
  private val conn = DriverManager.getConnection(url)

  override def clearTable(tableName: String, indexColumns: Option[List[Column]], columns: Option[List[Column]]): Unit = ???

  override def deleteTable(tableName: String): Unit = {
    val sql = conn.createStatement()

    val query = s"DROP TABLE IF EXISTS $tableName;"

    LOG.info(s"Deleting table $tableName (if it exists)...")
    sql.executeUpdate(query)
  }

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
      ", " + allColumns
        .filter(_.name != "id")
        .map(c => f"${c.name} ${c.`type` match {
          case ColumnType.TEXT => "VARCHAR(100)"
          case _ => c.`type`
        }}").mkString(", ")

    val query: String = s"CREATE TABLE $tableName (id VARCHAR(100) PRIMARY KEY $c)"

    LOG.info(s"Creating table $tableName...")
    sql.executeUpdate(query)

    if (indexColumns.isDefined) {
      indexColumns.get.foreach {
        ic =>
          val indexName = s"idx_${tableName.toLowerCase()}_${ic.name.toLowerCase()}"
          sql.executeUpdate(s"CREATE INDEX $indexName ON $tableName (${ic.name})")
          LOG.info(s"Created index $indexName")
      }
    }
  }

  override def putItem(id: String, tableName: String, fieldMap: Map[Column, AnyRef]): Option[String] = {
    val conn2 = DriverManager.getConnection(url)  // create new connection, ensure multi-threading
    val sql = conn2.createStatement()

    val query: String = _createInsertQuery(id, fieldMap, tableName)

    LOG.debug(query)
    LOG.info(s"Writing item with id $id to table $tableName...")

    Try {
      sql.executeUpdate(query)
      sql.close()
      conn2.close()
    } match {
      case Success(_) => Some(id)
      case Failure(e) =>
        LOG.error(s"Unable to write item with id $id to table $tableName", e)
        None
    }
  }

  override def putBatchItems(tableName: String, items: Array[Map[Column, AnyRef]]): Option[String] = ???

  override def deleteItem(id: String, tableName: String): Option[String] = ???

  override def putDomainEntity[T <: DomainEntity](tableName: String, entity: T): Option[String] = {
    val fields = entity.getFields(true, true)

    var id = entity.getId
    if ((id == null) || id.isEmpty) {
      id = createIDFor(tableName)
    }

    putItem(id, tableName, createColumns(fields))
  }

  private def _createInsertQuery(id: String, fieldMap: Map[Column, AnyRef], tableName: String): String = {
    val columns = "id, db_timestamp, " + fieldMap.filter(_._1.name != "id").map(_._1.name).mkString(", ")
    val values: String = _createItemValues(id, fieldMap)

    s"INSERT INTO $tableName ($columns) VALUES ($values);"
  }

  private def _createItemValues(id: String, fieldMap: Map[Column, AnyRef]) = {
    var values = s"'$id', "

    if (fieldMap.count(f => f._1.name == "db_timestamp") == 0) {
      val now = DateTime.now.toString("yyyy-MM-dd HH:mm:ss.SSS")
      values = values + s"'$now',  "
    }

    values = values + fieldMap.filter(_._1.name != "id").map {
      field =>
        field._1.`type` match {
          case ColumnType.TEXT => s"'${field._2}'"
          case ColumnType.INT => field._2
          case ColumnType.DOUBLE => field._2
          case ColumnType.BOOLEAN => field._2
          case ColumnType.TIMESTAMP => {
            field._2 match {
              case ts: Timestamp => DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(ts.toLocalDateTime)
              case dt: DateTime => dt.toString("yyyy-MM-dd HH:mm:ss.SSS")
              case l: java.lang.Long => new DateTime(l).toString("yyyy-MM-dd HH:mm:ss.SSS")
              case s: String => s"'$s'"  // assuming this is in the correct format
              case x =>
                LOG.error("Unknown date type in the Timestamp column")
                x.toString
            }
          }
        }
    }.mkString(", ")

    values
  }
}
