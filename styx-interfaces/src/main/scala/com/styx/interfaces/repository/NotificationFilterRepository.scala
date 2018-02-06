package com.styx.interfaces.repository

import com.styx.domain.NotificationFilter

import scala.concurrent.{ExecutionContext, Future}

trait NotificationFilterRepository {
  def addNotificationFilter(filter: NotificationFilter)(implicit ec:ExecutionContext): Future[Boolean]

  def getNotificationFilters(event: String)(implicit ec: ExecutionContext): Future[Seq[NotificationFilter]]
}
