package ai.styx.usecases.twitter

import ai.styx.common.Logging
import ai.styx.domain.events.{Trend, WordCount}
import org.apache.flink.streaming.api.scala.function.AllWindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.joda.time.DateTime

// in (timestamp of end of window, word, count), out (map time->trends), key (word), window
class TrendsWindowFunction(evaluationPeriodInSeconds: Int, dateTimePattern: String, topN: Int)
  extends AllWindowFunction[WordCount, Map[String, List[Trend]], TimeWindow] with Logging {

  def apply(window: TimeWindow, input: Iterable[WordCount], out: Collector[Map[String, List[Trend]]]): Unit = {

    val time = new DateTime(window.getEnd)
    val previousTime = time.minusSeconds(evaluationPeriodInSeconds)
    val previousMillis = previousTime.getMillis

    var topList = scala.collection.mutable.ListBuffer[Trend]()

    input
      // get the words in the last period that triggers the window
      .filter(_.timeStamp == window.getEnd)
      .foreach {
      wordCount =>
        // find pair in the previous period
        val previous = input.find(x => x.word == wordCount.word && x.timeStamp == previousMillis).getOrElse(WordCount(previousMillis, wordCount.word, 0))    // no word in previous period: set previous count = 0
        topList += Trend(s"${previousTime.toString(dateTimePattern)} to ${time.toString(dateTimePattern)}", wordCount.word, wordCount.count - previous.count)
    }

    out.collect(
      topList
        .groupBy(trend => trend.timeStamp)
        .map(trendPair => (trendPair._1, trendPair._2.sortBy(trend => -trend.slope).take(topN).toList))
    )
  }
}
