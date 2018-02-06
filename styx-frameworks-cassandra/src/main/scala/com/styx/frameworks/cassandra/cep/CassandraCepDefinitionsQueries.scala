package com.styx.frameworks.cassandra.cep

import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy
import com.datastax.driver.core.{ConsistencyLevel, PreparedStatement, Row, Statement}
import com.styx.domain.CepDefinition
import com.styx.frameworks.cassandra.CassandraRepository

class CassandraCepDefinitionsQueries(repo: CassandraRepository) {

  val get_cep_definitions: PreparedStatement = repo.session.prepare(
    "SELECT name, raw_event, business_event, criteria, model FROM cep WHERE raw_event=?;")

  def createCepDefinition(row: Row) = CepDefinition(
    Name = row.getString(0),
    RawEvent = row.getString(1),
    BusinessEvent = row.getString(2),
    Criteria = row.getString(3),
    Model = row.getString(4))

  def bindGetCepDefinitions(event: String): Statement = {
    get_cep_definitions.bind(
      event
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }
}
