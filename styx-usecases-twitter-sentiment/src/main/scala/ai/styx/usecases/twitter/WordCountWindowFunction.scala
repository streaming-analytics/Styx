package ai.styx.usecases.twitter

import ai.styx.common.Logging
import ai.styx.domain.events.WordCount
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

// in (word, count), out (timestamp of end of window, word, count), key (word), window
class WordCountWindowFunction extends WindowFunction[(String, Int), WordCount, String, TimeWindow] with Logging {
  def apply(key: String, window: TimeWindow, input: Iterable[(String, Int)], out: Collector[WordCount]): Unit = {
    val count = input.count(_ => true)
    out.collect(WordCount(window.getEnd, key, count))
  }
}
