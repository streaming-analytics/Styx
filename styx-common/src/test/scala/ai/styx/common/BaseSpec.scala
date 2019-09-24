package ai.styx.common

import java.sql.Timestamp

import org.joda.time.DateTime
import org.scalatest.{BeforeAndAfterAll, FlatSpec, GivenWhenThen, Matchers}

abstract class BaseSpec extends FlatSpec with Logging with GivenWhenThen with Matchers with BeforeAndAfterAll {
  def now: String = DateTime.now().toString("yyyyMMdd:HHmmSS")

  def stamp: Timestamp = new Timestamp(System.currentTimeMillis())
}
