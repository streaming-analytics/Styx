package ai.styx.domain

import java.sql.Timestamp
import java.time.Instant

import ai.styx.common.BaseSpec
import ai.styx.domain.events.Transaction
import org.joda.time.DateTime

class TransactionSpec extends BaseSpec {
  "Transaction" should "serialize to JSON" in {

    val d = new DateTime(2019, 11, 3, 16, 23, 21, 231)
    val i = Instant.ofEpochMilli(d.getMillis)
    val transaction = Transaction(
      Timestamp.from(i),
      "This is a test transaction",
      21.45,
    "EURO")
    val json = transaction.toJson()
    LOG.info(json)
    assert(json.contains("This is a test transaction"))
  }

  it should "deserialize from JSON" in {
    val json = "{\"time\":\"16-02-1972 08:59:01 +0000\",\"description\":\"This is another test transaction\",\"amount\":1441221.91,\"currency\":\"USD\"}"
    val transaction = Transaction.fromJson(json).get
    val c = transaction.created.get
    c.year().get() shouldBe 1972
    c.dayOfMonth().get() shouldBe 16
    c.minuteOfHour().get() shouldBe 59
    transaction.amount shouldBe 1441221.91
    transaction.description shouldBe "This is another test transaction"
  }
}
