package ai.styx.app.demo

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.Transaction
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringProducer}

import scala.util.Random

object KafkaTransactionsGenerator extends App with Logging {

  lazy val config: Configuration = Configuration.load()
  val topic: String = "payments" // Configuration.load().kafkaConfig.rawDataTopic

  val producer: KafkaStringProducer = KafkaFactory.createStringProducer(config.kafkaProducerProperties).asInstanceOf[KafkaStringProducer]

  val currency = "EURO"
  val location = "unknown"

  while (true) {
    val eventTime = new Timestamp(System.currentTimeMillis() - Random.nextInt(100))  // the event happened 0-100 ms in the past (for event time demo)

    val customerId = "Customer-" + Random.nextInt(100).toString  // simulate the data of 100 customers
    val amount = Random.nextDouble() * Random.nextInt(1000) * (if (Random.nextBoolean()) -1 else 1) * (if (Random.nextInt(1000) == 1) 1000 else 1)
    val counterAccount = "Company-" + Random.nextInt(1000).toString  // simulate 1000 companies to pay / get money from
    val description = Random.alphanumeric.take(10).mkString("")

    val t = Transaction(eventTime, description, amount, currency, location, customerId, counterAccount).toJson()
    producer.send(topic, t)

    Thread.sleep(10)  // 100 per second
    LOG.debug(s"Send transaction to topic $topic: " + t)
  }

  producer.close(1000L, TimeUnit.MILLISECONDS)
}
