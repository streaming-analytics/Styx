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

  var previousPage = scala.collection.mutable.Map[String, String]()
  for (c <- 0 to 9) {
    previousPage += (s"Customer_$c" -> "none")
  }

  while (true) {
    val eventTime = new DateTime(System.currentTimeMillis() - Random.nextInt(100))  // the event happened 0-100 ms in the past (for event time demo)

    val userId = if (Random.nextBoolean()) None else Some("Customer_" + Random.nextInt(10).toString)  // half of the url visits is a customer; simulate the data of 10 customers

    val url: String = if (userId.isDefined) {
      previousPage(userId.get) match {
        case "home" =>
          previousPage(userId.get) = "search"
          s"$mainUrl/$language/search" // one in ten URLs is a search
        case "search" =>
          previousPage(userId.get) = "product_1"
          s"$mainUrl/$language/products/${categories(Random.nextInt(5))}/${Random.nextInt(50000)}"
        case "product_1" =>
          previousPage(userId.get) = "product_2"
          s"$mainUrl/$language/products/${categories(Random.nextInt(5))}/${Random.nextInt(50000)}"
        case "product_2" =>
          previousPage(userId.get) = "product_3"
          s"$mainUrl/$language/products/${categories(Random.nextInt(5))}/${Random.nextInt(50000)}"
        case "product_3" =>
          previousPage(userId.get) = "cart" // after viewing 3 products, go to cart
          s"$mainUrl/$language/cart"
        case _ =>  // including "cart"
          previousPage(userId.get) = "home"
          s"$mainUrl/$language/home"
      }
    } else {
      Random.nextInt(10) match {
        case 0 => s"$mainUrl/$language/home" // one in ten URLs is the home page
        case 1 => s"$mainUrl/$language/search" // one in ten URLs is a search
        case 2 => s"$mainUrl/$language/cart" // one in ten URLs is a cart visit
        case _ => s"$mainUrl/$language/products/${categories(Random.nextInt(5))}/${Random.nextInt(50000)}" // the rest is product pages
      }
    }

    val ip = s"${Random.nextInt(256)}.${Random.nextInt(256)}.${Random.nextInt(256)}.${Random.nextInt(256)}"

    val c = Click(
      eventTime.toString("yyyy-MM-dd HH:mm:ss.SSS"),
      userId,
      url,
      ip,
      "UTC+01",
      "UK",
      RandomUserAgent.getRandomUserAgent,
      None, None, None, None, None, None
    ).toString
    producer.send(topic, c)

    Thread.sleep(100)  // 10 per second
    LOG.info(c)
  }

  producer.close(1000L, TimeUnit.MILLISECONDS)
}
