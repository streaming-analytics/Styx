package ai.styx.support.datagen

import ai.styx.common.LogOptionImplicit._
import ai.styx.common.{ConfigUtils, Logging}
import ai.styx.domain.kafka.TopicDefManager
import ai.styx.frameworks.kafka.StyxKafkaProducer

import scala.collection.JavaConverters._
import scala.util.Random

object DemoProducer extends Logging {
  val random: Random.type = Random

  def serialVersionUID: Long = 2174904787118597072L

  @volatile var running = true
  var i = 0L
  val maxSingleTransaction = 450

  val creator = new StaticDemoDataProducer()

  def cancel(): Unit = creator.cancel()

  def main(args: Array[String]) {

    println("Sending data...")
    random.setSeed(0)
    for (producer <- createProducer(args).logNone(logger.warn("Did not create producer, probably missing settings in config"))) {
      for (dataToSend <- creator.createData())
        producer.send(dataToSend)
      producer.waitForAllSendingToComplete()
    }
    logger.info("Producer shut down")
    println("Done.")
  }

  def createProducer(args: Array[String]): Option[StyxKafkaProducer] = {
    val config = ConfigUtils.loadConfig(args)
    val datagenConfig = config.getConfig("datagen")
    val writeConfig = datagenConfig.getConfig("write")
    val writeTopic = writeConfig.getString("topic")
    val allTopicDefs = TopicDefManager.getKafkaTopics(config.getConfig("kafka"))
    for (
      writeTopicDef <- allTopicDefs.get(writeTopic).logNone(logger.error(s"Could not find settings (protobuf, version, ...) in application.conf for topic $writeTopic"));
      brokerAddress <- Some(config.getStringList("prep.read.bootstrap.servers").asScala.mkString(","))
    ) yield new StyxKafkaProducer(brokerAddress, writeTopicDef)
  }
}
