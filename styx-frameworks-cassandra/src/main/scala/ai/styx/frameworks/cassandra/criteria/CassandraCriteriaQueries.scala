package ai.styx.frameworks.cassandra.criteria

import ai.styx.frameworks.cassandra.CassandraRepository
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy
import com.datastax.driver.core.{ConsistencyLevel, PreparedStatement, Row, Statement}
import ai.styx.domain.CriteriaDefinition
import ai.styx.frameworks.cassandra.CassandraRepository

class CassandraCriteriaQueries(repo: CassandraRepository) {

  val get_criteria_definitions: PreparedStatement = repo.session.prepare(
    "SELECT name, event, criteria FROM criteria_definitions WHERE event=?;")

  def createCriteriaDefinition(row: Row) = CriteriaDefinition(
    Name = row.getString(0),
    Event = row.getString(1),
    Criteria = row.getString(2))

  def bindGetCriteriaDefinitions(event: String): Statement = {
    get_criteria_definitions.bind(
      event
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }

  val add_criteria_definition: PreparedStatement = repo.session.prepare(
    "INSERT INTO criteria_definitions(name, event, criteria) VALUES (?,?,?);")

  def bindAddCriteriaDefinition(criteriaDefinition: CriteriaDefinition): Statement = {
    add_criteria_definition.bind(
      criteriaDefinition.Name,
      criteriaDefinition.Event,
      criteriaDefinition.Criteria
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }
}
