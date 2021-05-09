package ai.styx.usecases.clickstream

import java.util.Locale

import ai.styx.common.Logging
import ai.styx.domain.events.Click
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.watermark.Watermark
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

class ClickTimestampAndWatermarkGenerator extends AssignerWithPeriodicWatermarks[Click] with Logging {

  val maxOutOfOrderness = 100L // 0.1 seconds
  var currentMaxTimestamp: Long = 0L

  override def extractTimestamp(click: Click, previousElementTimestamp: Long): Long = {
    // format: 2017-07-01 01:11:12.634
    try {
      val timestamp = DateTime.parse(click.raw_timestamp, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withLocale(Locale.ENGLISH)).getMillis
      currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp)
      timestamp
    }
    catch {
      case t: Throwable =>
        LOG.error("Unable to extract event timestamp from click", t)
        0L
    }
  }

  override def getCurrentWatermark: Watermark = {
    // return the watermark as current highest timestamp minus the out-of-orderness bound
    new Watermark(currentMaxTimestamp - maxOutOfOrderness)
  }
}
