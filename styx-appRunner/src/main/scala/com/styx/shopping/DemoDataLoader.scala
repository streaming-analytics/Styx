package com.styx.shopping

import com.styx.common.Logging
import com.styx.support.datagen.DemoProducer

object DemoDataLoader extends Logging {

  def main(args: Array[String]): Unit = {
    logger.info("Starting data generator for demo.")

    val prod = DemoProducer
    prod.main(args)

    scala.io.StdIn.readLine("Press <enter>  to stop this program: .... ")
    prod.cancel()
    Runtime.getRuntime.exit(0)
  }
}
