package com.styx.frameworks.flink.connectors

import com.styx.domain.events.TimedEvent
import com.styx.common.Logging
import org.apache.flink.streaming.api.functions.{AssignerWithPeriodicWatermarks, AssignerWithPunctuatedWatermarks}
import org.apache.flink.streaming.api.watermark.Watermark

import scala.reflect.ClassTag

class TimedEventWatermarkExtractor[T <: TimedEvent : ClassTag]() extends AssignerWithPunctuatedWatermarks[T]() with Logging {

  @Override def checkAndGetNextWatermark(lastElement: T, extractedTimestamp: Long): Watermark = {
    new Watermark(extractedTimestamp)
  }

  @Override def extractTimestamp(element: T, previousElementTimestamp: Long): Long = {
    element.eventTime.getMillis   // no delay
  }
}

class PeriodicTimedEventWatermarkExtractor[T <: TimedEvent : ClassTag]() extends AssignerWithPeriodicWatermarks[T]() with Logging {

  override def extractTimestamp(element: T, previousElementTimestamp: Long): Long = {
    element.eventTime.getMillis
  }

  override def getCurrentWatermark: Watermark = {
    new Watermark(System.currentTimeMillis() - 1000)   // 1 second delay for processing of window
  }
}
