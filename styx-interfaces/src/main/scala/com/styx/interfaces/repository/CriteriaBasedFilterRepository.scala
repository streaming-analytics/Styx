package com.styx.interfaces.repository

import com.styx.domain.events.CriteriaFilter
import com.styx.domain.{CriteriaDefinition, events}
import com.styx.common.Logging

import scala.concurrent.{ExecutionContext, Future}

class CriteriaBasedFilterRepository(criteriaRepository: CriteriaRepository,
                                                 criteriaParser: CriteriaParser) extends CriteriaFilterRepository with Logging {
  def getCriteriaFilters(event: String)(implicit ec: ExecutionContext): Future[Seq[CriteriaFilter]] = {
    val filterDefinitionsQuery = criteriaRepository.getCriteriaDefinitions(event)
    filterDefinitionsQuery.map(_.map((nd: CriteriaDefinition) => {
      // TODO check parsing (check if criteria is parsed)
      val parsedCriteria = criteriaParser.parseCustomerCriteria(nd.Criteria)
      events.CriteriaFilter(nd.Name, nd.Event, parsedCriteria)
    }
    )
    )
  }

  override def addCriteriaFilter(businessEventDefinition: CriteriaFilter)(implicit ec: ExecutionContext): Future[Boolean] = ???
}
