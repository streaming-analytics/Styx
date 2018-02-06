package com.styx.config

import com.styx.common.ConfigUtils
import org.scalatest.{FunSpec, Matchers}

class PartiallyConfiguredConfigUtilsSpec extends FunSpec with Matchers {

  describe("ConfigUtils") {

    val styxGraphiteRoot: String = "graphite.root"
    val overrideConfigValue: String = "some_random_variable_set_for_config_test"

    describe("With no overrides/ additions") {
      it("should have repository") {
        ConfigUtils.loadConfig(Array()).hasPath("repository") should be(true)
      }
      it("should have graphite") {
        ConfigUtils.loadConfig(Array()).hasPath("graphite") should be(true)
      }
      it("should have kafka") {
        ConfigUtils.loadConfig(Array()).hasPath("kafka") should be(true)
      }
      it("should not have repository.password") {
        // Because it should be external of the main app
        // TODO
        //ConfigUtils.loadConfig(Array()).hasPath("repository.password") should be(false)
      }
      it("should not have override defined in different test") {
        ConfigUtils.loadConfig(Array()).hasPath("some.other.config.possible.override") should be(false)
        ConfigUtils.loadConfig(Array()).getAnyRef(styxGraphiteRoot) should not equal overrideConfigValue
      }
    }
    describe("With extra test_reference.conf") {
      it("should have repository.password") {
        // Because this application module should have one in the test resources
        ConfigUtils.loadConfig(Array("--config", "styx-app/src/test/resources/test_reference.conf")).hasPath("repository.password") should be(true)
      }
    }

    describe("With overrides Map") {

      val exampleOverride: Map[String, String] = Map("styx." + styxGraphiteRoot -> overrideConfigValue)

      it("should not have repository.password") {
        // TODO
        //ConfigUtils.loadConfig(overrides = exampleOverride).hasPath("repository.password") should be(false)
      }
      it("should still have kafka") {
        ConfigUtils.loadConfig(Array()).hasPath("kafka") should be(true)
      }
      it("should have an override set") {
        ConfigUtils.loadConfig(overrides = exampleOverride).getAnyRef(styxGraphiteRoot) should equal(overrideConfigValue)
      }
    }
  }

}
