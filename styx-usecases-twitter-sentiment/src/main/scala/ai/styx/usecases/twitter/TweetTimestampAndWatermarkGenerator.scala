package ai.styx.usecases.twitter

import ai.styx.common.Logging
import ai.styx.domain.events.Tweet
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.watermark.Watermark

class TweetTimestampAndWatermarkGenerator extends AssignerWithPeriodicWatermarks[Tweet] with Logging {

  val maxOutOfOrderness = 1000L // 1.0 seconds
  var currentMaxTimestamp: Long = 0L

  override def extractTimestamp(tweet: Tweet, previousElementTimestamp: Long): Long = {
    try {
      LOG.debug("Extracting timestamp...")
      if (tweet.created.isEmpty) {
        LOG.warn("No timestamp for tweet")
        0L
      }
      else {
        val timestamp = tweet.created.get.getMillis
        currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp)
        LOG.debug("Timestamp: " + timestamp)
        timestamp
      }
    }
    catch {
      case _: Throwable =>
        LOG.warn("Unable to extract timestamp from " + tweet.created_at)
        0L
    }
  }

  override def getCurrentWatermark: Watermark = {
    // return the watermark as current highest timestamp minus the out-of-orderness bound
    new Watermark(currentMaxTimestamp - maxOutOfOrderness)
  }
}
