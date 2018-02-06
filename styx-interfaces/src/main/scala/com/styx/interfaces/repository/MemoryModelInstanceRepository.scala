package com.styx.interfaces.repository

import com.styx.domain.models.ModelInstance

import scala.collection.parallel.mutable
import scala.concurrent.{ExecutionContext, Future}

class MemoryModelInstanceRepository extends ModelInstanceRepository{
  val models = mutable.ParHashMap[String, ModelInstance]()

  def getModel(name: String)(implicit ec: ExecutionContext): Future[ModelInstance] = Future{models(name)}

  def getModels(implicit ec: ExecutionContext): Future[Seq[ModelInstance]] = Future{models.values.toList}

  def addModel(name: String, model: ModelInstance)(implicit ec: ExecutionContext): Future[Boolean] =
    Future{
      models += name->model
      true
    }
}
