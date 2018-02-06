package com.styx.interfaces.repository

import com.styx.domain.NotificationFilter

import scala.collection.parallel.mutable
import scala.concurrent.{ExecutionContext, Future}

class MemoryNotificationFilterRepository() extends NotificationFilterRepository {
  val models = collection.mutable.Map[String, mutable.ParHashSet[NotificationFilter]]()

  def getNotificationFilters(event: String)(implicit ec: ExecutionContext): Future[Seq[NotificationFilter]] =
    Future {
      models.getOrElse(event, mutable.ParHashSet()).toList
    }

  def addNotificationFilter(filter: NotificationFilter)(implicit ec: ExecutionContext): Future[Boolean] =
    Future {
      val array = models.getOrElseUpdate(filter.Event, mutable.ParHashSet[NotificationFilter]())
      array += filter
      true
    }
}
