package com.styx.frameworks.flink.shopping

case class PoisonPillException(message: String = "", cause: Throwable = null) extends Exception (message, cause)
