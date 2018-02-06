package com.styx.frameworks.flink

import com.styx.domain.events.TimedEvent
import com.styx.common.Logging
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction.SourceContext

class SeqSourceFunction[T <: TimedEvent](source: Seq[T]) extends SourceFunction[T] with Logging {

  def cancel(): Unit = {}

  def run(sourceContext: SourceContext[T]): Unit = {
    for (testElement <- source) {
      logger.info(s"Created test data element $testElement")
      sourceContext.collectWithTimestamp(testElement, testElement.eventTime.getMillis)
    }
  }

}
