package com.styx.frameworks.cassandra.notifications

import com.datastax.driver.core.Statement
import com.styx.common.Logging
import com.styx.common.LogFutureImplicit._
import com.styx.frameworks.cassandra.CqlAttemptorImplicit._
import com.styx.domain.NotificationDefinition
import com.styx.interfaces.repository.NotificationRepository
import com.styx.frameworks.cassandra.CassandraRepository
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class CassandraNotificationRepository(repo: CassandraRepository) extends NotificationRepository with Logging {
  val statements = new CassandraNotificationQueries(repo)

  /**
    * Get all relevant notification definitions for a business event
    *
    * @param event The type of the business event, e.g. 'Shopping' or 'Abroad'
    */
  def getNotificationDefinitions(event: String)(implicit ec: ExecutionContext): Future[Seq[NotificationDefinition]] = {
    val statement: Statement = statements.bindGetNotificationDefinitions(event)
    repo.tryAsyncStatement(statement,
      "get_notification_definitions",
      s"get notification definitions for event $event")
      .map(_.all().asScala.map(statements.createNotificationDefinition))
      .logSuccess(definitions =>
        if (definitions.isEmpty)
          logger.warn(s"No notification definitions found for account event $event")
      )
  }

  def addNotificationDefinition(notificationDefinition: NotificationDefinition)(implicit ec: ExecutionContext): Future[Boolean] = {
    val statement: Statement = statements.bindAddNotificationDefinition(notificationDefinition)
    repo.tryAsyncStatement(statement,
      "add_notification_definitions",
      s"add notification definitions for event ${notificationDefinition.Event}")
      .map(_.wasApplied())
  }
}
