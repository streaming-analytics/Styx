package com.styx.frameworks.flink.connectors

import com.styx.domain.events.TimedEvent
import com.styx.common.Logging
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks
import org.apache.flink.streaming.api.watermark.Watermark

import scala.reflect.ClassTag

class TimedEventWatermarkExtractor[T <: TimedEvent : ClassTag]() extends AssignerWithPunctuatedWatermarks[T]() with Logging {

  @Override def checkAndGetNextWatermark(lastElement: T, extractedTimestamp: Long): Watermark = {
    new Watermark(extractedTimestamp)
  }

  @Override def extractTimestamp(element: T, previousElementTimestamp: Long): Long = {
    element.eventTime.getMillis
  }
}
