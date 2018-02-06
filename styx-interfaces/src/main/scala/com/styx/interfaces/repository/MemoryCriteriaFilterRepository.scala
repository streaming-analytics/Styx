package com.styx.interfaces.repository

import com.styx.domain.events.CriteriaFilter

import scala.collection.parallel.mutable
import scala.concurrent.{ExecutionContext, Future}

class MemoryCriteriaFilterRepository() extends CriteriaFilterRepository {
  val criteriafilters = collection.mutable.Map[String, mutable.ParHashSet[CriteriaFilter]]()

  def getCriteriaFilters(event: String)(implicit ec: ExecutionContext): Future[Seq[CriteriaFilter]] =
    Future {
      criteriafilters.getOrElse(event, mutable.ParHashSet()).toList
    }

  def addCriteriaFilter(filter: CriteriaFilter)(implicit ec: ExecutionContext): Future[Boolean] =
    Future {
      val array = criteriafilters.getOrElseUpdate(filter.Event, mutable.ParHashSet[CriteriaFilter]())
      array += filter
      true
    }
}
