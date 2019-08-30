package ai.styx.domain.utils

import ai.styx.common.Logging
import ai.styx.domain.utils.ColumnType.ColumnType

object ColumnType extends Enumeration with Logging {
  type ColumnType = Value
  val INT, TEXT, DOUBLE, BOOLEAN, TIMESTAMP = Value

  override def toString(): String = this.toString()

  def fromString(s: String): ColumnType = {
    s.toLowerCase match {
      case "string" => TEXT
      case "int" => INT
      case "integer" => INT
      case "double" => DOUBLE
      case "boolean" => BOOLEAN
      case "timestamp$" => TIMESTAMP
      case x =>
        LOG.debug("Unknown column type: " + x)  // most likely its an enumeration or a object type
        TEXT
    }
  }
}

case class Column(name: String, `type`: ColumnType)
