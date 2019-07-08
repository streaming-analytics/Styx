package ai.styx.usecases.twitter

import java.util.Locale

import ai.styx.domain.events.Tweet
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.watermark.Watermark
import org.joda.time._
import org.joda.time.format.DateTimeFormat

class TweetTimestampAndWatermarkGenerator extends AssignerWithPeriodicWatermarks[Tweet] {

  val maxOutOfOrderness = 1000L // 1.0 seconds
  var currentMaxTimestamp: Long = 0L

  override def extractTimestamp(tweet: Tweet, previousElementTimestamp: Long): Long = {
    // format: Sat Sep 10 22:23:38 +0000 2011
    try {
      val timestamp = DateTime.parse(tweet.creationDate, DateTimeFormat.forPattern("EE MMM dd HH:mm:ss Z yyyy").withLocale(Locale.ENGLISH)).getMillis
      currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp)
      timestamp
    }
    catch {
      case _: Throwable => 0L
    }
  }

  override def getCurrentWatermark: Watermark = {
    // return the watermark as current highest timestamp minus the out-of-orderness bound
    new Watermark(currentMaxTimestamp - maxOutOfOrderness)
  }
}
