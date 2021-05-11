package ai.styx.app.demo

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.Click
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringProducer}
import org.joda.time.DateTime

import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ListBuffer
import scala.util.Random

object KafkaClicksGenerator extends App with Logging {
  val BATCH_SIZE = 10
  val NUMBER_CUSTOMERS = 5

  lazy val config: Configuration = Configuration.load()
  val topic: String = "clicks"

  val producer: KafkaStringProducer = KafkaFactory.createStringProducer(config.kafkaProducerProperties).asInstanceOf[KafkaStringProducer]

  val tsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withLocale(Locale.ENGLISH)

  val mainUrl = "https://my_awesome_music_webshop.com"
  val language = "en-US"
  val categories = List("guitars", "pianos", "amplifiers", "headphones", "sheet_music")

  def createClick(delta: Int, ip: String, userId: Option[String], pageType: String, userAgent: String): String =
    Click(
      new DateTime(System.currentTimeMillis() - delta).toString("yyyy-MM-dd HH:mm:ss.SSS"),
      userId,
      s"$mainUrl/$language/$pageType",
      ip,
      "UTC+01",
      "UK",
      userAgent,
      None, None, None, None, None, None
    ).toString

  def simulateSession(userId: Option[String]): List[String] = {
    // simulate a series of clicks (page views) for 1 user
    val clicks = ListBuffer[String]()

    // session_id
    val ip = s"${Random.nextInt(256)}.${Random.nextInt(256)}.${Random.nextInt(256)}.${Random.nextInt(256)}"

    val userAgent = RandomUserAgent.getRandomUserAgent

    clicks += createClick(90, ip, userId, "login", userAgent)
    clicks += createClick(80, ip, userId, "home", userAgent)
    clicks += createClick(70, ip, userId, "search", userAgent)
    clicks += createClick(60, ip, userId, s"products/${categories(Random.nextInt(5))}/${Random.nextInt(50000)}", userAgent)
    clicks += createClick(50, ip, userId, s"products/${categories(Random.nextInt(5))}/${Random.nextInt(50000)}", userAgent)
    clicks += createClick(40, ip, userId, s"products/${categories(Random.nextInt(5))}/${Random.nextInt(50000)}", userAgent)
    clicks += createClick(30, ip, userId, "cart", userAgent)
    clicks += createClick(20, ip, userId, "logout", userAgent)

    clicks.toList
  }

  while (true) {
    // we generate blocks of x clicks at a time, and send them to Kafka together to randomize processing time.
    var clicks = ListBuffer[String]()

    for (i <- 1 to BATCH_SIZE) {
      // half of the url visits is a customer; simulate the data of x customers
      for (customer <- 1 to NUMBER_CUSTOMERS) {
        clicks ++= simulateSession(None)
        clicks ++= simulateSession(Some(s"Customer_$customer"))
      }
    }

    // clicks.foreach(LOG.info(_))

    LOG.info(s"Sending batch of ${BATCH_SIZE} sessions for ${NUMBER_CUSTOMERS} customers, ${clicks.length} clicks in total...")
    Random.shuffle(clicks).foreach{click => {
      producer.send(topic, click)
      Thread.sleep(Random.nextInt(1000) / NUMBER_CUSTOMERS / BATCH_SIZE / 2)
    }}
  }

  producer.close(1000L, TimeUnit.MILLISECONDS)
}
