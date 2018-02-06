package com.styx.frameworks.flink

import org.apache.flink.api.common.JobExecutionResult

import scala.collection.JavaConverters._

class TestResult(val result: JobExecutionResult) {
  def getResult[T](name: String): List[T] = result.getAccumulatorResult[java.util.ArrayList[T]](name).asScala.toList
}
