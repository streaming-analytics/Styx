package ai.styx.usecases.clickstream

import ai.styx.common.Logging
import ai.styx.domain.events.Click
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector

class CustomerSessionProcessWindowFunction extends ProcessWindowFunction[Click, String, String, TimeWindow] with Logging {
  override def process(key: String, context: Context, elements: Iterable[Click], out: Collector[String]): Unit = {
    val productViews = elements.count(_.rich_page_type.getOrElse("unknown") == "products")
    out.collect(s"Window for session ${key} passed, ${elements.size} clicks, ${productViews} products viewed")
  }
}
