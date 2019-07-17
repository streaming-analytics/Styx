package ai.styx.app.demo

import java.util.{Locale, Properties}
import java.util.concurrent.TimeUnit

import ai.styx.common.{ConfigUtils, Configuration, Logging}
import ai.styx.domain.events.Tweet
import ai.styx.frameworks.kafka.{KafkaProducerFactory, KafkaStringProducer}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.io.Source
import scala.util.Random

object KafkaDataGenerator extends App with Logging {

  lazy val config: Config = ConfigFactory.load()
  val writeProperties: Properties = ConfigUtils.propertiesFromConfig(config.getConfig("kafka.producer"))
  val topic: String = Configuration.load().kafkaConfig.topic

  val producer: KafkaStringProducer = KafkaProducerFactory.createStringProducer(writeProperties).asInstanceOf[KafkaStringProducer]

  val dataSourcePath = "sample.json"
  val lines = Source.fromResource(dataSourcePath).getLines()

  val tweets = scala.collection.mutable.ListBuffer[Tweet]()

  lines.foreach(line => {
    val tweet = Tweet.parse(line)
    if (tweet.isDefined && tweet.get.created.isDefined) tweets.append(tweet.get)
  })

  LOG.info(s"Loaded ${tweets.length} tweets into memory")

  producer.send(topic, "test 1234")

  val mapper: ObjectMapper = new ObjectMapper()
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
  mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)

  while (true) {
    val i = Random.nextInt(tweets.length - 1)

    val now = DateTime.now.toString(DateTimeFormat.forPattern("EE MMM dd HH:mm:ss Z yyyy").withLocale(Locale.ENGLISH))

    val tweet = tweets(i).copy(created_at = now).toJson(mapper)
    producer.send(topic, tweet)

    Thread.sleep(50)  // 20 per second
    LOG.info("Send tweet: " + tweet)
  }

  producer.close(1000L, TimeUnit.MILLISECONDS)
}
