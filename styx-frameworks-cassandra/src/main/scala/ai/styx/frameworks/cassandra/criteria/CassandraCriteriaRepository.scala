package ai.styx.frameworks.cassandra.criteria

import ai.styx.frameworks.cassandra.CassandraRepository
import com.datastax.driver.core.Statement
import ai.styx.common.LogFutureImplicit._
import ai.styx.common.Logging
import ai.styx.frameworks.cassandra.CqlAttemptorImplicit._
import ai.styx.domain.CriteriaDefinition

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class CassandraCriteriaRepository(repo: CassandraRepository) extends Logging {
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
          LOG.warn(s"No criteria definitions found for event $event")
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
