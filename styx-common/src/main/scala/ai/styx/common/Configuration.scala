package ai.styx.common

import com.typesafe.config.{Config, ConfigFactory}

class SparkConfiguration(config: Configuration) {
  private lazy val sparkConfig = config.getConfig("spark")

  lazy val master: String = sparkConfig.getString("master")
  lazy val appName: String = sparkConfig.getString("app.name")
  lazy val executorMemory: String = sparkConfig.getString("executor.memory")
  lazy val batchDuration: Int = sparkConfig.getInt("batch.duration")
  lazy val windowDuration: Int = sparkConfig.getInt("window.duration")
  lazy val slideDuration: Int = sparkConfig.getInt("slide.duration")
}

class KafkaConfiguration(config: Configuration) {
  private lazy val kafkaConfig = config.getConfig("kafka")

  lazy val bootstrapServers: String = kafkaConfig.getString("bootstrap.servers")
  lazy val groupId: String = kafkaConfig.getString("group.id")
  lazy val offsetReset: String = kafkaConfig.getString("offset.reset")
  lazy val topic: String = kafkaConfig.getString("topic")
}

class Configuration {
  private val _base: Config = ConfigFactory.load()

  /**
    * Returns a subtree of the base configuration with environment settings applied.
    *
    * @param path The subtree to return config for.
    * @return A config with base in given setting, with environment modifications applied.
    */
  def getConfig(path: String): Config = _base.getConfig(path)

  lazy val kafkaConfig: KafkaConfiguration = new KafkaConfiguration(this)
  lazy val sparkConfig: SparkConfiguration = new SparkConfiguration(this)
}

object Configuration {
  private var _config: Configuration = _

  def load(): Configuration = {
    _config = new Configuration()
    _config
  }

  def getConfig: Configuration = _config
}
