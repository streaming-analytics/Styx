package ai.styx.app.demo

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.Tweet
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringProducer}

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
    val maybeTweet = Tweet.fromJson(line)
    if (maybeTweet.isDefined && maybeTweet.get.created_at != null) {
      val tweet = maybeTweet.get
      val r = Random.nextInt(10)
      if (r < 3) {
        tweets.append(tweet.copy(text = tweet.text + " Kafka"))
      } else if (r < 5) {
        tweets.append(tweet.copy(text = tweet.text + " Spark"))
      } else if (r < 9) {
        tweets.append(tweet.copy(text = tweet.text + " Ignite"))
      } else {
        tweets.append(tweet)
      }
    }
  })

  LOG.info(s"Loaded ${tweets.length} tweets into memory")

  while (true) {
    val i = Random.nextInt(tweets.length - 1)

    val now = new Timestamp(System.currentTimeMillis())
    val tweet = tweets(i).copy(created_at = now).toJson()
    producer.send(topic, tweet)

    Thread.sleep(10)  // 100 per second
    //LOG.info("Send tweet: " + tweet)
  }

  producer.close(1000L, TimeUnit.MILLISECONDS)
}
