package com.styx.frameworks.flink

import org.apache.flink.api.common.accumulators.ListAccumulator
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector

class StreamCollector[T](name: String) extends RichFlatMapFunction[T, T] {

  override def open(parameters: Configuration) {
    getRuntimeContext.addAccumulator(name, new ListAccumulator[T])
  }

  def flatMap(in: T, collector: Collector[T]): Unit = {
    getRuntimeContext.getAccumulator[T, ListAccumulator[T]](name).add(in)
  }
}
