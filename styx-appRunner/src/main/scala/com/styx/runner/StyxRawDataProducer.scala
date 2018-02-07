package com.styx.runner

import com.styx.common.Logging
import com.styx.support.datagen.RawEventProducer

object StyxRawDataProducer extends Logging {

  def main(args: Array[String]): Unit = {
    val predefinedArgs = Array[String]()
    logger.info("Will now start the raw event producer")
    RawEventProducer.main(predefinedArgs ++ args)
    // could send poisonpill by overriding source file: styx.datagen.datafile
  }
}
