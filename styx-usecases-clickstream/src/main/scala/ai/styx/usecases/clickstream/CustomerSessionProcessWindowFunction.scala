package ai.styx.usecases.clickstream

import ai.styx.common.Logging
import ai.styx.domain.events.Click
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.windows.{GlobalWindow, TimeWindow}
import org.apache.flink.util.Collector

class CustomerSessionProcessWindowFunction extends ProcessWindowFunction[Click, String, String, TimeWindow] with Logging {
  override def process(key: String, context: Context, elements: Iterable[Click], out: Collector[String]): Unit = {

    val c = elements.toList.sortWith(_.raw_timestamp < _.raw_timestamp).map(click => s"${click.rich_page_type.get} -> ").mkString("")

    LOG.info(c)

    // sort?

    out.collect(s"Window for customer ${key} passed, ${elements.count(_ => true)} clicks")

  }
}
