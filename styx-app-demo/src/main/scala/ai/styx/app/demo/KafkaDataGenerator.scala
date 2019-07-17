package ai.styx.app.demo

import java.util.Properties
import java.util.concurrent.TimeUnit

import ai.styx.common.ConfigUtils
import ai.styx.frameworks.kafka.{KafkaProducerFactory, KafkaStringProducer}
import com.typesafe.config.{Config, ConfigFactory}

object KafkaDataGenerator extends App {

  lazy val config: Config = ConfigFactory.load()
  val writeProperties: Properties = ConfigUtils.propertiesFromConfig(config.getConfig("kafka"))

  val producer: KafkaStringProducer = KafkaProducerFactory.createStringProducer(writeProperties).asInstanceOf[KafkaStringProducer]

  producer.send("test 1234")

  producer.close(1000L, TimeUnit.MILLISECONDS)


  // loop

  // generate x tweets per second

  // select a random tweet from the tweets file

  // adjust the date (event time)

  // publish the tweet
}
