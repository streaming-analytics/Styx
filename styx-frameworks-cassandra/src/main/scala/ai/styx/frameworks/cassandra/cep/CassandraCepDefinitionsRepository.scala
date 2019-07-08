package ai.styx.frameworks.cassandra.cep

import ai.styx.frameworks.cassandra.CassandraRepository
import com.datastax.driver.core.Statement
import ai.styx.common.Logging
import ai.styx.common.LogTryImplicit._
import ai.styx.frameworks.cassandra.CqlAttemptorImplicit._
import ai.styx.domain.CepDefinition

import scala.collection.JavaConverters._

class CassandraCepDefinitionsRepository(repo: CassandraRepository) extends Logging {
  val statements = new CassandraCepDefinitionsQueries(repo)

  /**
    * Get all relevant CEP Engine definitions for a raw event
    *
    * @param event The type of raw event, e.g. 'UpdateCardBalance' or 'LocationUpdate'
    */
  def getCepDefinitions(event: String): Seq[CepDefinition] = {
    val statement: Statement = statements.bindGetCepDefinitions(event)
    repo.tryStatement(statement,
      "get_cep_definitions",
      s"get cep definitions for event $event")
      .map(_.all().asScala.map(statements.createCepDefinition))
      .logSuccess(definitions =>
        if (definitions.isEmpty)
          LOG.warn(s"No event definitions found for event $event")
      ).getOrElse(Seq.empty)
  }

}
