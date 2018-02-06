package com.styx.frameworks.cassandra.criteria

import com.datastax.driver.core.Statement
import com.styx.common.LogFutureImplicit._
import com.styx.common.Logging
import com.styx.frameworks.cassandra.CqlAttemptorImplicit._
import com.styx.domain.CriteriaDefinition
import com.styx.interfaces.repository.CriteriaRepository
import com.styx.frameworks.cassandra.CassandraRepository
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class CassandraCriteriaRepository(repo: CassandraRepository) extends CriteriaRepository with Logging {
  val statements = new CassandraCriteriaQueries(repo)

  /**
    * Get all relevant criteria definitions for a business event
    *
    * @param event The type of the business event, e.g. 'Shopping' or 'Abroad'
    */
  def getCriteriaDefinitions(event: String)(implicit ec: ExecutionContext): Future[Seq[CriteriaDefinition]] = {
    val statement: Statement = statements.bindGetCriteriaDefinitions(event)
    repo.tryAsyncStatement(statement,
      "get_criteria_definitions",
      s"get criteria definitions for event $event")
      .map(_.all().asScala.map(statements.createCriteriaDefinition))
      .logSuccess(definitions =>
        if (definitions.isEmpty)
          logger.warn(s"No criteria definitions found for event $event")
      )
  }

  def addCriteriaDefinition(criteriaDefinition: CriteriaDefinition)(implicit ec: ExecutionContext): Future[Boolean] = {
    val statement: Statement = statements.bindAddCriteriaDefinition(criteriaDefinition)
    repo.tryAsyncStatement(statement,
      "add_criteria_definitions",
      s"add criteria definitions for event ${criteriaDefinition.Event}")
      .map(_.wasApplied())
  }
}
