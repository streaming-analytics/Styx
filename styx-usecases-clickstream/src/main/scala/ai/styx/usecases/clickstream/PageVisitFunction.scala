package ai.styx.usecases.clickstream

import ai.styx.domain.events.Click
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import java.util.Locale

class PageVisitFunction extends RichFlatMapFunction[Click, (String, DateTime)] {
  val typeInfoStringDateTime: TypeInformation[(String, DateTime)] = TypeInformation.of(classOf[(String, DateTime)])

  private var previousPage: ValueState[(String, DateTime)] = _

  override def flatMap(input: Click, out: Collector[(String, DateTime)]): Unit = {
    val timestamp = DateTime.parse(input.raw_timestamp, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withLocale(Locale.ENGLISH))
    val page = input.rich_page_type.getOrElse("unknown")

    previousPage.update(page, timestamp)

    out.collect(previousPage.value())
  }

  override def open(parameters: Configuration): Unit = {
    previousPage = getRuntimeContext.getState(
      new ValueStateDescriptor[(String, DateTime)]("previous page", typeInfoStringDateTime)
    )
  }
}
