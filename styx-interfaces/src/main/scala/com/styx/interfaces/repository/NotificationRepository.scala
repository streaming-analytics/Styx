package com.styx.interfaces.repository

import com.styx.domain.NotificationDefinition

import scala.concurrent.{ExecutionContext, Future}

trait NotificationRepository {
  /**
    * Get all relevant notification definitions for a business event
    * @param event The type of the business event, e.g. 'Shopping' or 'Abroad'
    */
  def getNotificationDefinitions(event: String)(implicit ec: ExecutionContext): Future[Seq[NotificationDefinition]]

  def addNotificationDefinition(notificationDefinition: NotificationDefinition)(implicit ec: ExecutionContext): Future[Boolean]

}
