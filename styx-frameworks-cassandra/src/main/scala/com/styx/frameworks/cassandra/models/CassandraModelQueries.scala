package com.styx.frameworks.cassandra.models

import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy
import com.datastax.driver.core.{ConsistencyLevel, PreparedStatement, Row, Statement}
import com.styx.domain.PmmlModel
import com.styx.frameworks.cassandra.CassandraRepository

class CassandraModelQueries(repo: CassandraRepository) {
  val add_model: PreparedStatement = repo.session.prepare(
    "INSERT INTO models(name, pmml) VALUES (?, ?);")

  val get_model: PreparedStatement = repo.session.prepare(
    "SELECT name, blobastext(pmml) FROM models WHERE name=?;")

  val get_models: PreparedStatement = repo.session.prepare(
    "SELECT name, blobastext(pmml) FROM models;")

  def createModel(row: Row) = PmmlModel(
    Name = row.getString(0),
    Pmml = row.getString(1))

  def bindGetModel(name: String): Statement = {
    get_model.bind(
      name
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }

  def bindGetModels: Statement = {
    get_models.bind.setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }

  def bindAddModel(name: String, pmml: String): Statement = {
    get_model.bind(
      name,
      pmml
    ).setIdempotent(true).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).setRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
  }
}
