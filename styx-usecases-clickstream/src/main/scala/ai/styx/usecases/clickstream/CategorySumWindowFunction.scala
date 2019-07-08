package ai.styx.usecases.clickstream

import org.apache.flink.streaming.api.scala.function.AllWindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

class CategorySumWindowFunction(topN: Int) extends AllWindowFunction[CategoryCount, List[CategoryCount], TimeWindow] {

  def apply(window: TimeWindow, input: Iterable[CategoryCount], out: Collector[List[CategoryCount]]): Unit = {

    var topList = scala.collection.mutable.ListBuffer[CategoryCount]()

    input
      // get the categories in the last period that triggers the window
      .filter(_.timeStamp == window.getEnd)
      .foreach {
        categoryCount =>
          topList += categoryCount
      }

    out.collect(
      topList
        .sortBy(s => s.count)(Ordering[Int].reverse)
        .take(topN)
        .toList
    )
  }
}
