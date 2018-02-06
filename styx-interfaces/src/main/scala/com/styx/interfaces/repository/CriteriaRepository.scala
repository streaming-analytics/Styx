package com.styx.interfaces.repository

import com.styx.domain.CriteriaDefinition

import scala.concurrent.{ExecutionContext, Future}

trait CriteriaRepository {
  /**
    * Get all relevant criteria definitions for a business event
    * @param event The type of the business event, e.g. 'Shopping' or 'Abroad'
    */
  def getCriteriaDefinitions(event: String)(implicit ec: ExecutionContext): Future[Seq[CriteriaDefinition]]

  def addCriteriaDefinition(businessEventDefinition: CriteriaDefinition)(implicit ec: ExecutionContext): Future[Boolean]

}
