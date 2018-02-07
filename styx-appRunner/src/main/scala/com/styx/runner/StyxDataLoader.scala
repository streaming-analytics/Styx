package com.styx.runner

import com.styx.common.Logging

object StyxDataLoader extends Logging {

  def main(args: Array[String]): Unit = {
    val predefinedArgs = Array[String]() // ("--topic", "updatecardbalancetesttopic16", "--bootstrap.servers", "localhost:9092", "--zookeeper.connect", "localhost:2181", "--group.id", "styx--consumergroup")
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
