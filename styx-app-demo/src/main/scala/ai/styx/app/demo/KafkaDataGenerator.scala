package ai.styx.app.demo

import java.sql.Timestamp
import java.util.Locale
import java.util.concurrent.TimeUnit

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.Tweet
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringProducer}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.io.Source
import scala.util.Random

object KafkaDataGenerator extends App with Logging {

  lazy val config: Configuration = Configuration.load()
  val topic: String = Configuration.load().kafkaConfig.rawDataTopic

  val producer: KafkaStringProducer = KafkaFactory.createStringProducer(config.kafkaProducerProperties).asInstanceOf[KafkaStringProducer]

  // load data file
  val dataSourcePath = "sample.json"
  val lines = Source.fromResource(dataSourcePath).getLines()
  val tweets = scala.collection.mutable.ListBuffer[Tweet]()

  lines.foreach(line => {
    val tweet = Tweet.fromJson(line)
    if (tweet.isDefined && tweet.get.created_at != null) tweets.append(tweet.get)
  })

  LOG.info(s"Loaded ${tweets.length} tweets into memory")

  while (true) {
    val i = Random.nextInt(tweets.length - 1)
    //val now = DateTime.now.toString(DateTimeFormat.forPattern("EE MMM dd HH:mm:ss Z yyyy").withLocale(Locale.ENGLISH))

    val now = new Timestamp(System.currentTimeMillis())
    val tweet = tweets(i).copy(created_at = now).toJson()
    producer.send(topic, tweet)

    Thread.sleep(5)  // 200 per second
    //LOG.info("Send tweet: " + tweet)
  }

  producer.close(1000L, TimeUnit.MILLISECONDS)
}
