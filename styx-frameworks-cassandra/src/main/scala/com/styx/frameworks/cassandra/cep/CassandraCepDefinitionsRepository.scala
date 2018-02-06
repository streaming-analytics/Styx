package com.styx.frameworks.cassandra.cep

import com.datastax.driver.core.Statement
import com.styx.common.Logging
import com.styx.common.LogTryImplicit._
import com.styx.frameworks.cassandra.CqlAttemptorImplicit._
import com.styx.domain.CepDefinition
import com.styx.interfaces.repository.CepDefinitionRepository
import com.styx.frameworks.cassandra.CassandraRepository

import scala.collection.JavaConverters._

class CassandraCepDefinitionsRepository(repo: CassandraRepository) extends CepDefinitionRepository with Logging {
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
          logger.warn(s"No event definitions found for event $event")
      ).getOrElse(Seq.empty)
  }

}
