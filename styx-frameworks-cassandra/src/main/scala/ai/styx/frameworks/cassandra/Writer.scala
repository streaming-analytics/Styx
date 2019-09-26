package ai.styx.frameworks.cassandra

import ai.styx.domain.DomainEntity
import ai.styx.domain.utils.{Column, ColumnType}
import ai.styx.frameworks.interfaces.DatabaseWriter
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}


class Writer(node: String, port: Int, keyspace: String, tablePrefix: String) extends DatabaseWriter {
  val connector = new CassandraConnector()
  connector.connect(node, Some(port), keyspace)
  val session = connector.getSession

  override def clearTable(tableName: String, indexColumns: Option[List[Column]] = None, columns: Option[List[Column]] = None): Unit = {
    deleteTable(tableName)
    createTable(tableName, indexColumns, columns)
  }

  override def deleteTable(tableName: String): Unit = {
    val tableNameWithPrefix = s"${tablePrefix}_$tableName"

    val query: String = s"DROP TABLE IF EXISTS $tableNameWithPrefix"

    LOG.info(s"Deleting table $tableNameWithPrefix...")
    session.execute(query)
  }

  override def createTable(tableName: String, indexColumns: Option[List[Column]], columns: Option[List[Column]]): Unit = {
    val tableNameWithPrefix = s"${tablePrefix}_$tableName"

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

    val query: String = s"CREATE TABLE IF NOT EXISTS $tableNameWithPrefix (id TEXT PRIMARY KEY $c)"

    LOG.info(s"Creating table $tableNameWithPrefix...")
    session.execute(query)

    if (indexColumns.isDefined) {
      indexColumns.get.foreach {indexColumn =>
        val indexQuery: String = s"CREATE INDEX ON $tableNameWithPrefix (${indexColumn.name});"

        LOG.info(s"Creating index on column ${indexColumn.name} of table $tableNameWithPrefix...")
        session.execute(indexQuery)
      }
    }
  }

  override def putItem(id: String, tableName: String, fieldMap: Map[Column, AnyRef]): Option[String] = {
    val tableNameWithPrefix = s"${tablePrefix}_$tableName"

    val query: String = _createInsertQuery(id, fieldMap, tableNameWithPrefix)

    LOG.debug(query)
    LOG.info(s"Writing item with id $id to table $tableNameWithPrefix...")

    Try {
      session.execute(query)
    } match {
      case Success(_) => Some(id)
      case Failure(e) =>
        LOG.error(s"Unable to write item with id $id to table $tableNameWithPrefix", e)
        None
    }
  }

  override def putBatchItems(tableName: String, items: Array[Map[Column, AnyRef]]): Option[String] = {
    val tableNameWithPrefix = s"${tablePrefix}_$tableName"

    LOG.info(s"Adding ${items.length} items as a batch to $tableNameWithPrefix...")

    Try {
      val query = new StringBuilder()
      query.append("BEGIN BATCH \n")

      val idColumn = Column("id", ColumnType.TEXT)
      items.foreach{item =>
        query.append(_createInsertQuery(item.get(idColumn).get.toString, item, tableNameWithPrefix))
        query.append("\n")
      }

      query.append("APPLY BATCH;")

      LOG.info(query.toString)

      session.execute(query.toString)
    } match {
      case Success(_) => Some("OK")
      case Failure(t) => LOG.error("Unable to write batch of items to table " + tableNameWithPrefix, t)
        return None
    }
  }

  override def deleteItem(id: String, tableName: String): Option[String] = {
    val tableNameWithPrefix = s"${tablePrefix}_$tableName"

    val query: String = s"DELETE FROM $tableNameWithPrefix WHERE id = '$id';"

    LOG.info(s"Deleting item with id $id from table $tableNameWithPrefix...")

    Try {
      session.execute(query)
    } match {
      case Success(_) => Some(id)
      case Failure(e) =>
        LOG.error(s"Unable to delete item with id $id from table $tableNameWithPrefix", e)
        None
    }
  }

  override def putDomainEntity[T <: DomainEntity](tableName: String, entity: T): Option[String] = {
    val fields = entity.getFields(true, true)

    var id = entity.getId
    if ((id == null) || id.isEmpty) {
      val tableNameWithPrefix = s"${tablePrefix}_$tableName"
      id = createIDFor(tableNameWithPrefix)
    }

    putItem(id, tableName, createColumns(fields))
  }

  private def _createInsertQuery(id: String, fieldMap: Map[Column, AnyRef], tableNameWithPrefix: String): String = {
    val columns = "id, db_timestamp, " + fieldMap.filter(_._1.name != "id").map(_._1.name).mkString(", ")
    val values: String = _createItemValues(id, fieldMap)

    s"INSERT INTO $tableNameWithPrefix ($columns) VALUES ($values);"
  }

  private def _createItemValues(id: String, fieldMap: Map[Column, AnyRef]) = {
    var values = s"'$id', "

    if (fieldMap.count(f => f._1.name == "db_timestamp") == 0) {
      val now = DateTime.now.getMillis
      values = values + s"$now,  "
    }

    values = values + fieldMap.filter(_._1.name != "id").map {
      field =>
        field._1.`type` match {
          case ColumnType.TEXT => s"'${field._2}'"
          case ColumnType.INT => field._2
          case ColumnType.LONG => field._2
          case ColumnType.DOUBLE => field._2
          case ColumnType.BOOLEAN => field._2
          case ColumnType.TIMESTAMP => {
            field._2 match {
              case dt: DateTime => dt.getMillis  // TODO: check
              case l: java.lang.Long => l
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
