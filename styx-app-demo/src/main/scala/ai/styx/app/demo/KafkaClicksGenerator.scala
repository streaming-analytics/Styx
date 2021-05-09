package ai.styx.app.demo

import ai.styx.app.demo.KafkaTransactionsGenerator.LOG
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

  while (true) {
    val eventTime = new DateTime(System.currentTimeMillis() - Random.nextInt(100))  // the event happened 0-100 ms in the past (for event time demo)

    val customerId = "Customer-" + Random.nextInt(100).toString  // simulate the data of 100 customers
    val amount = Random.nextDouble() * Random.nextInt(1000) * (if (Random.nextBoolean()) -1 else 1) * (if (Random.nextInt(1000) == 1) 1000 else 1)
    val counterAccount = "Company-" + Random.nextInt(1000).toString  // simulate 1000 companies to pay / get money from
    val description = Random.alphanumeric.take(10).mkString("")

    val c = Click(
      eventTime.toString("yyyy-MM-dd HH:mm:ss.SSS"),
      "user_1",
    ).toString
    producer.send(topic, c)

    Thread.sleep(1000)  // 100 per second
    LOG.debug(s"Send click to topic $topic: " + c)
  }

  producer.close(1000L, TimeUnit.MILLISECONDS)
}
