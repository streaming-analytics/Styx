package ai.styx.usecases.fraud

import ai.styx.domain.events.Transaction
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.watermark.Watermark

class TransactionTimestampAndWatermarkGenerator extends AssignerWithPeriodicWatermarks[Transaction] {
  val maxOutOfOrderness = 1000L // 1 second
  var currentMaxTimestamp: Long = 0L

  override def extractTimestamp(element: Transaction, previousElementTimestamp: Long): Long = {
    currentMaxTimestamp = Math.max(element.time.getTime, currentMaxTimestamp)
    element.time.getTime
  }

  override def getCurrentWatermark: Watermark =
    new Watermark(currentMaxTimestamp - maxOutOfOrderness)
}
