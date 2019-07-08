package ai.styx.common

import org.joda.time.DateTime
import org.scalatest.{FlatSpec, GivenWhenThen, Matchers}

abstract class BaseSpec extends FlatSpec with Logging with GivenWhenThen with Matchers {
  def now: String = DateTime.now().toString("yyyyMMdd:HHmmSS")
}
