package ai.styx.frameworks.ignite

import ai.styx.common.{BaseSpec, Configuration}
import ai.styx.domain.events.TweetWindowTrend
import ai.styx.domain.utils.{Column, ColumnType}
import ai.styx.frameworks.interfaces.{DatabaseFetcher, DatabaseWriter}

class WriterSpec extends BaseSpec with EmbeddedIgnite {

  val dbFactory = new IgniteFactory(Configuration.load().igniteConfig.url)
  val dbWriter: DatabaseWriter = dbFactory.createWriter
  val dbFetcher: DatabaseFetcher = dbFactory.createFetcher

  val trendsColumns = List(Column("id", ColumnType.TEXT), Column("windowStart", ColumnType.TIMESTAMP), Column("windowEnd", ColumnType.TIMESTAMP), Column("word", ColumnType.TEXT), Column("count", ColumnType.INT))

  override def beforeAll(): Unit = {
    super.beforeAll()

    dbWriter.deleteTable("trends")
    dbWriter.createTable("trends", None, Some(trendsColumns))
  }

  "Ignite Writer" should "create a database table" in {
    // covered by main function
  }

  it should "write a domain entity record" in {
    val t = TweetWindowTrend("id_test_1", stamp, stamp, "testword", 21)
    dbWriter.putDomainEntity("trends", t)

    val item = dbFetcher.getItem("id_test_1", "trends")
    assert(item.isDefined)
    assert(item.get("WORD").toString == "testword")
  }
}
