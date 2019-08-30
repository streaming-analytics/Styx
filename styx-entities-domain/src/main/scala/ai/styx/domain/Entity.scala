package ai.styx.domain

import ai.styx.common.Logging
import ai.styx.domain.utils.{Column, ColumnType, Timestamp}

import scala.collection.mutable.ListBuffer

abstract class Entity extends Logging {
  // def toJson(unsafe: Boolean = false): Json  // TODO: circe Json

  def getColumns(includeId: Boolean = true, flatten: Boolean = false): List[Column] = {
    val fields = this.getFields(includeId, flatten).+("db_timestamp" -> Timestamp)

    fields.map(field => Column(field._1, ColumnType.fromString{
      if (field._2 == null) "string" else field._2.getClass.getSimpleName})).toList
  }

  def getFields(includeId: Boolean = true, flatten: Boolean = false): collection.immutable.Map[String, AnyRef] = {
    // Don't expose internal fields
    val c = this.getClass

    val treeMap = c.getDeclaredFields.filter(f => !f.getName.startsWith("_internal_")).map {
      // TODO: use shapeless
      field =>
        field.setAccessible(true)

        val name =
          if (!includeId && field.getName == "id")
            "original_id"
          else
            field.getName

        val value = field.get(this)

        val mapValue = value match {
          case o: Option[AnyRef] => o.getOrElse("None") // TODO: None (type, no string)
          case e: Entity => e.getFields(includeId, flatten)
          case x => x
        }

        (name, mapValue)
    }.toMap[String, AnyRef]

    if (flatten) {
      // 0. Create a mutable copy of treeMap: flattenedMap
      // 1. Find fields with map values (the 'map-fields') in the treeMap
      // 2. for each map value of the map-fields found, prefix keys with field name of the map-field itself
      // 3. concatenate each map found in to the new flattenedMap
      // 4. delete map-fields from flattenedMap

      // 0. Create a mutable copy of treeMap: flattenedMap
      val flattenedMap = collection.mutable.Map[String, AnyRef]() ++= treeMap

      // 1. Find fields with map values (the 'map-fields') in the treeMap
      val mapFields = ListBuffer[String]()
      treeMap.foreach(p => p._2 match {
        case _: collection.immutable.Map[String, AnyRef] => mapFields += p._1
        case _ => Unit
      })

      mapFields.foreach(f => {
        // 2. for each map value of the map-fields found, prefix keys with field name of the map-field itself
        val mapValue = treeMap(f).asInstanceOf[collection.immutable.Map[String, AnyRef]]
        val prefixedMapValue = collection.mutable.Map[String, AnyRef]()

        mapValue.foreach(p => prefixedMapValue(s"${f}_${p._1}") = p._2)

        // 3. concatenate each map found in to the new flattenedMap
        flattenedMap ++= prefixedMapValue

        // 4. delete map-fields from flattenedMap
        flattenedMap -= f
      })

      // Make immutable
      flattenedMap.toMap

    } else {
      treeMap
    }
  }
}
