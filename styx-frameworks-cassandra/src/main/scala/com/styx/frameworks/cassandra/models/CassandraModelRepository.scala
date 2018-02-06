package com.styx.frameworks.cassandra.models

import com.datastax.driver.core.Statement
import com.styx.common.Logging
import com.styx.common.LogFutureImplicit._
import com.styx.frameworks.cassandra.CqlAttemptorImplicit._
import com.styx.domain.PmmlModel
import com.styx.frameworks.cassandra.CassandraRepository
import com.styx.frameworks.openscoring.repository.ModelRepository

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class CassandraModelRepository(repo: CassandraRepository) extends ModelRepository with Logging {
  val statements = new CassandraModelQueries(repo)

  /**
    * Get a PMML model by name
    */
  def getModel(name: String)(implicit ec: ExecutionContext): Future[PmmlModel] = {
    val statement: Statement = statements.bindGetModel(name)
    repo.tryAsyncStatement(statement,
      "get_model",
      s"get pmml model with name $name")
      .map(result => statements.createModel(result.one))
      .logFailure(t => logger.error(s"No model found with name $name: ${t.getMessage}"))
  }

  def getModels(implicit ec: ExecutionContext): Future[Seq[PmmlModel]] = {
    val statement: Statement = statements.bindGetModels
    repo.tryAsyncStatement(statement,
      "get_models",
      "get all pmml models from database")
      .map(_.all().asScala.map(statements.createModel))
      .logSuccess(models =>
        if (models.isEmpty) logger.warn("No PMML models found in database"))
  }

  def addModel(name: String, pmmlModel: PmmlModel)(implicit ec: ExecutionContext): Future[Boolean] = {
    val statement = statements.bindAddModel(name, pmmlModel.Pmml)
    repo.tryAsyncStatement(statement,
      "add_model",
      s"insert pmml model with name $name")
      .map(_.wasApplied())
      .logFailure(t => logger.error(s"Could not add model with name $name: ${t.getMessage}"))
  }
}
