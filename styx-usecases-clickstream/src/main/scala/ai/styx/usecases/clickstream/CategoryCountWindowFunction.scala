package ai.styx.usecases.clickstream

import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

class CategoryCountWindowFunction extends WindowFunction[(String, Int), CategoryCount, String, TimeWindow] {
  def apply(key: String, window: TimeWindow, input: Iterable[(String, Int)], out: Collector[CategoryCount]): Unit = {
    val count = input.count(_ => true)
    out.collect(CategoryCount(window.getEnd, key, count))
  }
}
