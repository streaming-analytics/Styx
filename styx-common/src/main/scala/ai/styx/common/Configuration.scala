package ai.styx.common

import java.util.Properties
import com.typesafe.config.{Config, ConfigFactory, ConfigValueType}
import scala.collection.JavaConverters._

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

  // TODO: remove the following, they are injected directly in the Kafka producer/consumer
  lazy val bootstrapServers: String = kafkaConfig.getString("bootstrap.servers")
  lazy val groupId: String = kafkaConfig.getString("group.id")
  lazy val offsetReset: String = kafkaConfig.getString("offset.reset")

  lazy val rawDataTopic: String = kafkaConfig.getString("rawDataTopic")
  lazy val patternTopic: String = kafkaConfig.getString("patternTopic")

  // TODO: consumer and producer
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
  lazy val kafkaConsumerProperties: Properties = Configuration.propertiesFromConfig(getConfig("kafka.consumer"))
  lazy val kafkaProducerProperties: Properties = Configuration.propertiesFromConfig(getConfig("kafka.producer"))

  lazy val sparkConfig: SparkConfiguration = new SparkConfiguration(this)
}

object Configuration {
  private var _config: Configuration = _

  def load(): Configuration = {
    _config = new Configuration()
    _config
  }

  def getConfig: Configuration = _config

  def propertiesFromMap(properties: Map[String, String]): Properties =
    (new Properties /: properties) {
      case (a, (k, v)) =>
        a.put(k, v)
        a
    }

  def propertiesFromConfig(config: Config): Properties = {
    propertiesFromMap(config.entrySet().asScala.map(entry => {
      val value = entry.getValue
      val valueStr = value.valueType() match {
        case ConfigValueType.OBJECT => value.unwrapped().toString
        case ConfigValueType.LIST => value.unwrapped().toString.stripPrefix("[").stripSuffix("]")
        case ConfigValueType.NUMBER => value.unwrapped().toString
        case ConfigValueType.BOOLEAN => value.unwrapped().toString
        case ConfigValueType.NULL => value.unwrapped().toString
        case ConfigValueType.STRING => value.unwrapped().toString
      }
      entry.getKey -> valueStr
    }).toMap)
  }
}
