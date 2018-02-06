package com.styx.shopping

import com.styx.common.LogFutureImplicit._
import com.styx.common.Logging
import com.styx.shopping.StyxAppKafkaLessJob

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object StyxFileAppRunner extends Logging {

  def main(args: Array[String]): Unit = {
    val predefinedArgs = Array[String]() // ("--topic", "updatecardbalancetesttopic16", "--bootstrap.servers", "dnl-chsv-kafka-tst-1.europe.intranet:9092", "--zookeeper.connect", "dnl-chsv-zk-kafka-tst-1.europe.intranet:2181", "--group.id", "styx--consumergroup")
    val flink = Future {
      StyxAppKafkaLessJob.main(predefinedArgs ++ args)
    }
    flink.logFailure(e => logger.error("Failed to run flink job", e))
    logger.info("Started flink processing.")
    logger.info("This specific job creates the raw events itself")
    logger.info("Waiting a few seconds to let flink get the jobs running, and to process the events..")
    Thread.sleep(12 * 1000)
    logger.warn("Done, now would be safe to kill the jvm")
    scala.io.StdIn.readLine("Press <enter>  to stop this program: .... ")
    Runtime.getRuntime.exit(0)
  }
}

