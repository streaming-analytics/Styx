package com.styx.frameworks.flink.shopping

import com.styx.common.LogTryImplicit._
import com.styx.domain.Customer
import com.styx.domain.events.BusinessEvent._
import com.styx.domain.events.{BaseBusinessEvent, CriteriaFilter}
import com.styx.common.Logging
import org.apache.flink.api.common.functions.RichMapFunction

import scala.util.Try

class CriteriaFilterFunction() extends RichMapFunction[(BaseBusinessEvent, Customer, Seq[CriteriaFilter]), Option[BaseBusinessEvent]] with Logging {

  private def decideInclusionOfBusinesseventOnFilter(filter: CriteriaFilter, profile:Customer): Boolean = {
    filter.Criteria(profile) match {
      case Right(result) => result
      case Left(_) => false
    }
  }

  def map(event: (BaseBusinessEvent, Customer, Seq[CriteriaFilter])): Option[BaseBusinessEvent] = event match {
    case (businessEvent, customerProfile, businessEventFilters) =>
      logger.info("Found customer")
      // 3. apply selection criteria for each businessEvent for customer
      logger.info(s"Verifying the ${businessEventFilters.size} businessEvent filters.")

      Try {
        val filtered =
          for (filter <- businessEventFilters if decideInclusionOfBusinesseventOnFilter(filter, customerProfile))
            yield businessEvent

        filtered.headOption
          .map(_.addTimeStamp("FILTER"))
      }.logFailure(e=>logger.error("Failed to filter", e))
        .getOrElse(None)
  }
}