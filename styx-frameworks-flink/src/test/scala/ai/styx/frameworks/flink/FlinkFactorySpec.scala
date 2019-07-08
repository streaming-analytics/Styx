package ai.styx.frameworks.flink

import ai.styx.common.BaseSpec

class FlinkFactorySpec extends BaseSpec {
  "Flink Factory" should "start up Flink" in {
    FlinkFactory.i shouldBe 1
  }
}
