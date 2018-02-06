package com.styx.interfaces.repository

import com.styx.domain.events.CriteriaFilter

import scala.concurrent.{ExecutionContext, Future}

trait CriteriaFilterRepository {
  def addCriteriaFilter(filter: CriteriaFilter)(implicit ec:ExecutionContext): Future[Boolean]

  def getCriteriaFilters(event: String)(implicit ec: ExecutionContext): Future[Seq[CriteriaFilter]]
}
