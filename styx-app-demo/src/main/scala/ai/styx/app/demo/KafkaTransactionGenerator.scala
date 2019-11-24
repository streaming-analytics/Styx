package ai.styx.app.demo

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.Transaction
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringProducer}

import scala.util.Random

object KafkaTransactionGenerator extends App with Logging {

  lazy val config: Configuration = Configuration.load()
  val topic: String = "transactions"

  val producer: KafkaStringProducer = KafkaFactory.createStringProducer(config.kafkaProducerProperties).asInstanceOf[KafkaStringProducer]

  while (true) {

    val now = new Timestamp(System.currentTimeMillis())
    val transaction = new Transaction(now, "test transaction", Random.nextInt(1000) + Random.nextDouble(), "EURO")

    producer.send(topic, transaction.toJson())

    Thread.sleep(5)  // 100 per second
    LOG.info("Send transaction: " + transaction)
  }

  producer.close(1000L, TimeUnit.MILLISECONDS)
}
