package ai.styx.frameworks.ignite

import ai.styx.common.BaseSpec
import ai.styx.domain.utils.{Column, ColumnType}

class WriterSpec extends BaseSpec with EmbeddedIgnite {

  val writer = new Writer("jdbc:ignite:thin://127.0.0.1")

  "Ignite Writer" should "create a database table" in {
    writer.createTable("Test1", None, Some(List(Column("name", ColumnType.TEXT))))
  }
}
