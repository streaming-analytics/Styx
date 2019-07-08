package ai.styx.frameworks.openscoring.repository

import ai.styx.domain.PmmlModel

import scala.concurrent.{ExecutionContext, Future}

trait ModelRepository {
  def addModel(name: String, pmmlModel: PmmlModel)(implicit ec: ExecutionContext): Future[Boolean]
  def getModel(name: String)(implicit ec: ExecutionContext): Future[PmmlModel]
  def getModels(implicit ec: ExecutionContext): Future[Seq[PmmlModel]]
}
