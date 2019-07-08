package ai.styx.support.datagen

import ai.styx.common.LogOptionImplicit._
import ai.styx.common.LogTryImplicit._
import ai.styx.common.UsingImplicit._
import ai.styx.common.{ConfigUtils, Logging}
import ai.styx.domain.kafka.{TopicDef, TopicDefManager}
import ai.styx.frameworks.kafka.StyxKafkaProducerFactory
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.{Callback, ProducerRecord, RecordMetadata}

class StyxCallback extends Callback with Logging {
  override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
    if (exception != null) {
      logger.error("Event not send to eventbus due to error: " + exception.getMessage, exception)
    } else {
      if (metadata.offset % 1000 == 0) print(".")  // 10000
    }
  }
}

object RawEventProducer extends Logging {

  def main(args: Array[String]): Unit = {
    val config = ConfigUtils.loadConfig(args)

    val allTopicDefs = TopicDefManager.getKafkaTopics(config.getConfig("kafka"))

    logger.info("Now will put raw events on kafka")
    writeDataTokafka(config.getConfig("datagen"), allTopicDefs)
    logger.info("Finished putting all raw events on kafka.")
  }

  def writeDataTokafka(config: Config, allTopicDefs: Map[String, TopicDef]): Unit = {
    val writeConfig = config.getConfig("write")

    val maxRows: Option[Int] = if (config.hasPath("num")) Some(config.getInt("num")) else None
    val filename = config.getString("datafile")
    logger.info(s"Sending data from $filename...")

    val generator = new RawEventGenerator(maxRows, filename)

    for (
      writeTopic <- Some(writeConfig.getString("topic"));
      writeTopicDef <- allTopicDefs.get(writeTopic).logNone(logger.error(s"Could not find settings (protobuf, version, ...) in application.conf for topic $writeTopic"))
    ) yield {
      usingTry(StyxKafkaProducerFactory.getProducer(writeTopicDef, ConfigUtils.propertiesFromConfig(writeConfig), writeConfig.getString("client.id"))) {
        kp => {
          generator.createData().foreach(line => {
            val r = new ProducerRecord(writeTopicDef.fullyQualifiedName, line.get("CARD_ID").toString, line.toString)
            kp.send(r, new StyxCallback)
          })
        }
      }.logFailure(error => logger.error("Sending data to Kafka failed: ", error))
      logger.info("Producer shut down")
      println("Done.")
    }
  }

}
