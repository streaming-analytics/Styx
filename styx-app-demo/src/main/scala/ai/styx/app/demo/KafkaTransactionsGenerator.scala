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

  while (true) {
    val now = new Timestamp(System.currentTimeMillis())
    val customerId = "Customer-" + Random.nextInt(10).toString
    val amount = Random.nextDouble() * Random.nextInt(1000)  // TODO: add negatives
    val counterAccount = "Customer-" + Random.nextInt(10).toString

    val t = Transaction(now, "description", amount, "EURO", "somewhere", customerId, counterAccount).toJson()
    producer.send(topic, t)

    Thread.sleep(10)  // 100 per second
    LOG.info(s"Send transaction to topic $topic: " + t)
  }

  producer.close(1000L, TimeUnit.MILLISECONDS)
}
