package ai.styx.usecases.clickstream

import ai.styx.common.Logging
import ai.styx.domain.events.Click
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.util.Collector


class PageVisitFunction extends KeyedProcessFunction[String, Click, (Click, Click, Long)] with Logging {
  val typeInfo: TypeInformation[(Click, Click, Long)] = TypeInformation.of(classOf[(Click, Click, Long)])

  // current, previous, current (event) time
  private var currentAndPreviousPage: ValueState[(Click, Click, Long)] = _

  // initialization
  override def open(parameters: Configuration): Unit = {
    currentAndPreviousPage = getRuntimeContext.getState(
      new ValueStateDescriptor[(Click, Click, Long)]("current and previous page", typeInfo)
    )
  }

  override def processElement(current: Click, ctx: KeyedProcessFunction[String, Click, (Click, Click, Long)]#Context, out: Collector[(Click, Click, Long)]): Unit = {
    val currentTimestamp = ctx.timestamp()
    // or DateTime.parse(current.raw_timestamp, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withLocale(Locale.ENGLISH)).getMillis

    val page = current.rich_page_type.getOrElse("unknown")

    if (currentAndPreviousPage == null || currentAndPreviousPage.value() == null) {
      currentAndPreviousPage.update(current, null, 0L)
      return
    }

    val previousPage = currentAndPreviousPage.value()._1
    val previousTimestamp = currentAndPreviousPage.value()._3
    val diff = currentTimestamp - previousTimestamp

    if (page == "cart") {
      // current page is the cart; let's see if the previous page was indeed a product page
      if (previousPage.rich_page_type.getOrElse("unknown") != "products") {
        // strange; the cart is visited _before_ a product page
        LOG.warn("========================= INCORRECT USER FLOW =========================")
      }

      if (diff < 0) {
        // strange; the previous page is _after_ the current page
        LOG.warn("====================== INCORRECT ORDER OF EVENTS ======================")
      }

      // update the state
      currentAndPreviousPage.update(current, previousPage, currentTimestamp)

      // send the current, previous, duration back to be processed further downstream
      out.collect(currentAndPreviousPage.value())
    }
  }
}

//class PageVisitFunctionOLD extends RichFlatMapFunction[Click, (Click, Click, Long)] with Logging {
//  val typeInfo: TypeInformation[(Click, Click, Long)] = TypeInformation.of(classOf[(Click, Click, Long)])
//
//  // current, previous, time between
//  private var currentAndPreviousPage: ValueState[(Click, Click, Long)] = _
//
//  // initialization
//  override def open(parameters: Configuration): Unit = {
//    currentAndPreviousPage = getRuntimeContext.getState(
//      new ValueStateDescriptor[(Click, Click, Long)]("current and previous page", typeInfo)
//    )
//  }
//
//  // in:  current click
//  // out: current click, previous click, time between visits
//  // NOTE: we keep the current click to be able to integrate this in our stream; in essence it's an enrichment function
//  override def flatMap(current: Click, out: Collector[(Click, Click, Long)]): Unit = {
//    val currentTimestamp = DateTime.parse(current.raw_timestamp, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withLocale(Locale.ENGLISH))
//    val page = current.rich_page_type.getOrElse("unknown")
//
//    if (currentAndPreviousPage == null || currentAndPreviousPage.value() == null) {
//      currentAndPreviousPage.update(current, null, 0L)
//      return
//    }
//
//    val previousPage = currentAndPreviousPage.value()._1
//    val previousTimestamp = DateTime.parse(currentAndPreviousPage.value()._1.raw_timestamp, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withLocale(Locale.ENGLISH))
//    val diff = currentTimestamp.getMillis - previousTimestamp.getMillis
//
//    if (page == "cart") {
//      // current page is the cart; let's see if the previous page was indeed a product page
//      if (previousPage.rich_page_type.getOrElse("unknown") != "products") {
//        // strange; the cart is visited _before_ a product page
//        LOG.warn("========================= INCORRECT USER FLOW =========================")
//      }
//
//      if (diff < 0) {
//        // strange; the previous page is _after_ the current page
//        LOG.warn("====================== INCORRECT ORDER OF EVENTS ======================")
//      }
//
//      // update the state
//      currentAndPreviousPage.update(current, previousPage, diff)
//
//      // send the current, previous, duration back to be processed further downstream
//      out.collect(currentAndPreviousPage.value())
//    }
//  }
//}
