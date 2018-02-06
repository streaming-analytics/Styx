package com.styx.frameworks.flink.datagenerators

import com.styx.common.Logging
import org.apache.flink.streaming.api.functions.source.SourceFunction

abstract class RandomEventSourceFunction[T](val Gender: Option[Int] = None, val delayMs: Int = 100) extends SourceFunction[T] with Logging  {

}
