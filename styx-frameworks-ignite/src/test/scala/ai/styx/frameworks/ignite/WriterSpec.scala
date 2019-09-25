package ai.styx.frameworks.ignite

import ai.styx.common.{BaseSpec, Configuration}
import ai.styx.domain.events.TweetWindowTrend
import ai.styx.domain.utils.{Column, ColumnType}
import ai.styx.frameworks.interfaces.{DatabaseFetcher, DatabaseWriter}

class WriterSpec extends BaseSpec with EmbeddedIgnite {

  val dbFactory = new IgniteFactory(Configuration.load().igniteConfig.url)
  val dbWriter: DatabaseWriter = dbFactory.createWriter
  val dbFetcher: DatabaseFetcher = dbFactory.createFetcher

  val trendsColumns = List(Column("id", ColumnType.TEXT), Column("windowId", ColumnType.INT), Column("windowStart", ColumnType.TIMESTAMP), Column("windowEnd", ColumnType.TIMESTAMP), Column("word", ColumnType.TEXT), Column("count", ColumnType.INT))

  override def beforeAll(): Unit = {
    super.beforeAll()

    dbWriter.deleteTable("trends")
    dbWriter.createTable("trends", None, Some(trendsColumns))
  }

  "Ignite Writer" should "create a database table" in {
    // covered by main function
  }

  it should "write and fetch a domain entity record" in {
    val t = TweetWindowTrend("id_test_1", 1, stamp, stamp, "testword", 21)
    dbWriter.putDomainEntity("trends", t)

    val item = dbFetcher.getItem("id_test_1", "trends")
    assert(item.isDefined)
    assert(item.get("WORD").toString == "testword")
  }

  it should "fetch a domain entity based on a where clause" in {
    val t = TweetWindowTrend("id_test_2", 21, stamp, stamp, "testword", 98)
    dbWriter.putDomainEntity("trends", t)

    val items = dbFetcher.getItems("windowId", "21", "trends")
    assert(items.isDefined)
    assert(items.get.length == 1)
    assert(items.get.head("WORD").toString == "testword")
  }
}
