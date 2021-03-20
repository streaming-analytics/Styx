package ai.styx.common

import java.sql.Timestamp
import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

abstract class BaseSpec extends AnyFlatSpec with Logging with GivenWhenThen with Matchers with BeforeAndAfterAll {
  def now: String = DateTime.now().toString("yyyyMMdd:HHmmSS")

  def stamp: Timestamp = new Timestamp(System.currentTimeMillis())
}
