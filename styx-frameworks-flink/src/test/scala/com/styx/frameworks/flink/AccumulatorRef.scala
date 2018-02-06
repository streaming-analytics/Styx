package com.styx.frameworks.flink

case class AccumulatorRef[T](name: String) {
  def allFrom(result: TestResult): List[T] = result.getResult[T](name)

}
