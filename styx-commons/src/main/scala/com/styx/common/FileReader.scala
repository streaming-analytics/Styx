package com.styx.common

import scala.io.{BufferedSource, Source}

object FileReader extends Logging {

  def loadResource(filename: String): BufferedSource = {
    val stream = getClass.getResourceAsStream(filename)
    Source.fromInputStream(stream)
  }
}

