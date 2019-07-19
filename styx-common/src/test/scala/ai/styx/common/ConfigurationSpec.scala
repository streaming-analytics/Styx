package ai.styx.common

class ConfigurationSpec extends BaseSpec {
  "Configuration" should "read config properties" in {
    val config = Configuration.load()

    config.kafkaConfig.rawDataTopic shouldBe "test1"
    config.kafkaConsumerProperties.getProperty("topic") shouldBe "test2"
  }
}
