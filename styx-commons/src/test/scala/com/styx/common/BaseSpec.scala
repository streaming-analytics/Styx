package com.styx.common

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.easymock.EasyMockSugar
import org.scalatest.{FlatSpec, GivenWhenThen, Matchers}

abstract class BaseSpec extends FlatSpec with Matchers with GivenWhenThen with ScalaFutures with EasyMockSugar with Logging {
}
