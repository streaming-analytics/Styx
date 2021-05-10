package ai.styx.app.demo

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.Click
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringProducer}
import org.joda.time.DateTime

import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import scala.util.Random

object KafkaClicksGenerator extends App with Logging {

  lazy val config: Configuration = Configuration.load()
  val topic: String = "clicks"

  val producer: KafkaStringProducer = KafkaFactory.createStringProducer(config.kafkaProducerProperties).asInstanceOf[KafkaStringProducer]

  val tsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withLocale(Locale.ENGLISH)

  val mainUrl = "https://my_awesome_music_webshop.com"
  val language = "en-US"
  val categories = List("guitars", "pianos", "amplifiers", "headphones", "sheet_music")

  while (true) {
    val eventTime = new DateTime(System.currentTimeMillis() - Random.nextInt(100))  // the event happened 0-100 ms in the past (for event time demo)

    val userId = if (Random.nextBoolean()) None else Some("Customer-" + Random.nextInt(10).toString)  // half of the url visits is a customer; simulate the data of 10 customers

    // one in ten URLs is a cart visit, the other are home, search or product detail pages
    val url = {
      Random.nextInt(10) match {
        case 0 => s"$mainUrl/$language/home"
        case 1 => s"$mainUrl/$language/search"
        case 2 => s"$mainUrl/$language/cart"
        case _ => s"$mainUrl/$language/${categories(Random.nextInt(5))}/${Random.nextInt(50000)}"
        }
      }

    val ip = s"${Random.nextInt(256)}.${Random.nextInt(256)}.${Random.nextInt(256)}.${Random.nextInt(256)}"

    val userAgent = "Mozilla/5.0 (Android 4.4; Tablet; rv:41.0) Gecko/41.0 Firefox/41.0"

    val c = Click(
      eventTime.toString("yyyy-MM-dd HH:mm:ss.SSS"),
      userId,
      url,
      ip,
      "UTC+01",
      "UK",
      userAgent,
      None, None, None, None, None, None
    ).toString
    producer.send(topic, c)

    Thread.sleep(1000)  // 100 per second
    LOG.info(s"Send click to topic $topic: " + c)
  }

  producer.close(1000L, TimeUnit.MILLISECONDS)
}
