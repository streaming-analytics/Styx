package com.styx.frameworks.cassandra.notifications

import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy
import com.datastax.driver.core.{ConsistencyLevel, PreparedStatement, Row, Statement}
import com.styx.domain.NotificationDefinition
import com.styx.frameworks.cassandra.CassandraRepository

class CassandraNotificationQueries(repo: CassandraRepository) {

  val get_notification_definitions: PreparedStatement = repo.session.prepare(
    "SELECT name, event, model, threshold, message FROM notification_definitions WHERE event=?;")

  def createNotificationDefinition(row: Row) = NotificationDefinition(
    Name = row.getString(0),
    Event = row.getString(1),
    Model = row.getString(3),
    Threshold = row.getDouble(4),
    Message = row.getString(5))

  def bindGetNotificationDefinitions(event: String): Statement = {
    get_notification_definitions.bind(
      event
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }

  val add_notification_definition: PreparedStatement = repo.session.prepare(
    "INSERT INTO notification_definitions(name, event, model, threshold, message) VALUES (?,?,?,?,?,?);")

  def bindAddNotificationDefinition(notificationDefinition: NotificationDefinition): Statement = {
    add_notification_definition.bind(
      notificationDefinition.Name,
      notificationDefinition.Event,
      notificationDefinition.Model,
      notificationDefinition.Threshold.asInstanceOf[Object],
      notificationDefinition.Message
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }
}
