package com.styx.interfaces.repository

import com.styx.domain.NotificationFilter
import com.styx.common.Logging

import scala.concurrent.{ExecutionContext, Future}

class ModelBasedNotificationFilterRepository(modelInstanceRepository: ModelInstanceRepository,
                                             notificationRepo: NotificationRepository) extends NotificationFilterRepository with Logging {
  def getNotificationFilters(event: String)(implicit ec: ExecutionContext): Future[Seq[NotificationFilter]] = {
    val notificationDefinitionsQuery = notificationRepo.getNotificationDefinitions(event)
    notificationDefinitionsQuery.flatMap(ndSeq => Future.sequence(
      ndSeq.map(nd => {
        val modelFuture = modelInstanceRepository.getModel(nd.Model)
        for (model <- modelFuture) yield {
          // TODO check if this can be simplified
          NotificationFilter(nd.Name, nd.Event, model, nd.Threshold, nd.Message)
        }
      })))
  }

  def addNotificationFilter(filter: NotificationFilter)(implicit ec: ExecutionContext): Future[Boolean] = ???
}
