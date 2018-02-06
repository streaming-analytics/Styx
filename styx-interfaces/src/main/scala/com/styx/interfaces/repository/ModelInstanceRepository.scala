package com.styx.interfaces.repository

import com.styx.domain.models.ModelInstance

import scala.concurrent.{ExecutionContext, Future}


trait ModelInstanceRepository {
  def addModel(name: String, model: ModelInstance)(implicit ec: ExecutionContext): Future[Boolean]
  def getModel(name: String)(implicit ec: ExecutionContext): Future[ModelInstance]
  def getModels(implicit ec: ExecutionContext): Future[Seq[ModelInstance]]
}
