package com.styx.shopping

import com.styx.common.Logging
import com.styx.frameworks.cassandra.CustomerProfileReader

object StyxDataLoader extends Logging {

  def main(args: Array[String]): Unit = {
    val predefinedArgs = Array[String]() // ("--topic", "updatecardbalancetesttopic16", "--bootstrap.servers", "dnl-chsv-kafka-tst-1.europe.intranet:9092", "--zookeeper.connect", "dnl-chsv-zk-kafka-tst-1.europe.intranet:2181", "--group.id", "styx--consumergroup")
    logger.info("Starting import.")
    //    val loader = Future{
    //      CustomerProfileReader.main(predefinedArgs ++ args)
    //    }
    CustomerProfileReader.main(predefinedArgs ++ args)
    //    loader.logFailure(e=>logger.error("Failed to run load job", e))
    //    logger.warn("Done, now would be safe to kill the jvm")
    //    scala.io.StdIn.readLine("Press <enter>  to stop this program: .... ")
    Runtime.getRuntime.exit(0)
  }
}
