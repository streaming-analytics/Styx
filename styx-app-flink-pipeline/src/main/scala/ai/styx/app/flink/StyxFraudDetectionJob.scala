package ai.styx.app.flink

import ai.styx.common.{Configuration, Logging}
import ai.styx.domain.events.Transaction
import ai.styx.frameworks.kafka.{KafkaFactory, KafkaStringConsumer, KafkaStringProducer}
import ai.styx.usecases.fraud.TransactionTimestampAndWatermarkGenerator
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object StyxFraudDetectionJob extends App with Logging {
  // configuration
  implicit val config: Configuration = Configuration.load()

  LOG.info("Start!")

  // set up Flink
  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

  implicit val typeInfo1: TypeInformation[Transaction] = TypeInformation.of(classOf[Transaction])
  implicit val typeInfo2: TypeInformation[Option[Transaction]] = TypeInformation.of(classOf[Option[Transaction]])
  implicit val typeInfo3: TypeInformation[Option[String]] = TypeInformation.of(classOf[Option[String]])
  implicit val typeInfo4: TypeInformation[String] = TypeInformation.of(classOf[String])
  implicit val typeInfo5: TypeInformation[(String, Int)] = TypeInformation.of(classOf[(String, Int)])

  val producer = KafkaFactory.createStringProducer(config.kafkaProducerProperties).asInstanceOf[KafkaStringProducer]

  val rawEventsStream = env
    .addSource(KafkaFactory.createMessageBusConsumer(config).asInstanceOf[KafkaStringConsumer])

  // part 1: just log it
  val transactionStream = rawEventsStream.map(s => Transaction.fromString(s))
  // transactionStream.addSink(transaction => LOG.info(s"Received transaction of ${transaction.amount} ${transaction.currency} from ${transaction.customerId} to ${transaction.counterAccount} at time ${transaction.time}, description: ${transaction.description}"))

  // part 2: filter extremely large transactions
  val largeAmountsStream = transactionStream.filter(t => t.amount > 100000 || t.amount < -100000)
  largeAmountsStream.addSink(transaction => LOG.info(s"Found suspicious transaction of ${transaction.amount} ${transaction.currency} from ${transaction.customerId} to ${transaction.counterAccount} at time ${transaction.time}, description: ${transaction.description}"))

  // part 3: key by customer
  // transactionStream.assignTimestampsAndWatermarks(new TransactionTimestampAndWatermarkGenerator())
  // transactionStream.keyBy(t => t.customerId)

  // part 4: CEP --> check for unusual transaction counts per hour
  // .countWindowAll(1000L)  // count the transactions per second

  // part 5: ML --> compare the transactions to customer context
  // val notificationsEventsStream = businessEventsStream

  env.execute("Fraud detection")

  LOG.info("Done!")

}
