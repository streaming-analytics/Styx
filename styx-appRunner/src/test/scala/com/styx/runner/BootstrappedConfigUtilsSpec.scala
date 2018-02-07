package com.styx.runner

import com.styx.common.ConfigUtils
import org.scalatest.{FunSpec, Matchers}

class BootstrappedConfigUtilsSpec extends FunSpec with Matchers {

  describe("ConfigUtils"){
    describe("With no overrides/ additions") {
      it("should have repository.password") {
        // Because this bootstrap module provides one in the classpath
        ConfigUtils.loadConfig(Array()).hasPath("repository.password") should be(true)
      }
    }
    describe("With the normal config provided as extra reference.conf"){
      it("should still have repository.password") {
        ConfigUtils.loadConfig(Array("--config", "/reference.conf")).hasPath("repository.password") should be(true)
      }
    }
  }

}
