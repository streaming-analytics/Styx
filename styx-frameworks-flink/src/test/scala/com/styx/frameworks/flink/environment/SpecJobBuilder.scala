package com.styx.frameworks.flink.environment

import java.util.UUID

import com.styx.frameworks.flink.{AccumulatorRef, StreamCollector, TestResult}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

case class SpecJobBuilder(env: StreamExecutionEnvironment) extends AnyVal{

  def execute(): Option[TestResult] = {
    val e = env.execute("test")
    Some(new TestResult(e))
  }

  def withEventAccumulator[T](stream: DataStream[T])(implicit evidence$8: org.apache.flink.api.common.typeinfo.TypeInformation[T]): Option[AccumulatorRef[T]] = {
    val accumulatorName = UUID.randomUUID().toString
    stream.flatMap(new StreamCollector[T](accumulatorName))
    Some(AccumulatorRef[T](accumulatorName))
  }

}
