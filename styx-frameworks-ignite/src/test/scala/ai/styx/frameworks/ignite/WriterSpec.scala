package ai.styx.frameworks.ignite

import ai.styx.common.BaseSpec
import ai.styx.domain.events.TweetWindowTrend
import ai.styx.domain.utils.{Column, ColumnType}

class WriterSpec extends BaseSpec with EmbeddedIgnite {

  val writer = new Writer("jdbc:ignite:thin://127.0.0.1")

  val columns = List(Column("id", ColumnType.TEXT), Column("windowStart", ColumnType.TIMESTAMP), Column("windowEnd", ColumnType.TIMESTAMP), Column("word", ColumnType.TEXT), Column("count", ColumnType.INT))
  //writer.createTable("trends", None, Some(columns))

  override def beforeAll(): Unit = {
    super.beforeAll()

    writer.deleteTable("trends")
    writer.createTable("trends", None, Some(columns))
  }

  "Ignite Writer" should "create a database table" in {
    // covered by main function
  }

  it should "write a domain entity record" in {
    val t = TweetWindowTrend("id_test_1", stamp, stamp, "testword", 21)
    writer.putDomainEntity("trends", t)
  }
}
