package com.styx.frameworks.openscoring

import java.io.ByteArrayInputStream

import com.styx.common.LogFutureImplicit._
import com.styx.common.Logging
import com.styx.interfaces.repository.ModelInstanceRepository
import com.styx.domain.PmmlModel
import com.styx.domain.models.ModelInstance
import com.styx.frameworks.openscoring.repository.ModelRepository
import org.dmg.pmml.Model
import org.jpmml.evaluator.{ModelEvaluator, ModelEvaluatorFactory}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class PmmlModelInstanceRepository(modelRepository: ModelRepository) extends ModelInstanceRepository with Logging {

  @transient private lazy val modelEvaluatorFactory: ModelEvaluatorFactory = ModelEvaluatorFactory.newInstance()

  // all models that are available, loaded into memory from the resources folder
  private lazy val models: mutable.Map[String, PmmlModelInstance] = load

  /**
    * (re)load the pmml models from disk / database
    *
    * @return
    */
  private def load: mutable.Map[String, PmmlModelInstance] = {
    //val models = modelRepository.getModels
    logger.info("Loading PMML models...")

    val modelFuture =
      modelRepository.getModels
        .map(
          _.map(m => {
            val modelEvaluator = loadModel(m)
            m.Name -> modelEvaluator
          }))
        .map(items => mutable.Map(items: _*))
    Await.result(modelFuture
      .logFailure(e => logger.error("Failed to get models.", e))
      .recover { case _ => mutable.Map[String, PmmlModelInstance]() }, Duration.Inf)
  }

  private def loadModel(m: PmmlModel): PmmlModelInstance = {
    val stream = new ByteArrayInputStream(m.Pmml.getBytes)
    val pmml = org.jpmml.model.PMMLUtil.unmarshal(stream)
    logger.info("PMML model loaded: " + m.Name)

    val modelEvaluator: ModelEvaluator[_ <: Model] = modelEvaluatorFactory.newModelEvaluator(pmml)
    modelEvaluator.verify()

    logger.info("PMML model verified!")
    new PmmlModelInstance(modelEvaluator)
  }

  def getModel(modelName: String)(implicit ec: ExecutionContext): Future[PmmlModelInstance] = Future {
    models.getOrElseUpdate(modelName, {
      logger.warn(s"Trying to load newly requested model with name $modelName")
      Await.result(modelRepository.getModel(modelName).map(loadModel), Duration.Inf) //throws exception, causing the update to fail
      // thus a failing name will be retried on each call
    })
  }

  def getModels(implicit ec: ExecutionContext): Future[Seq[ModelInstance]] = Future {
    models.values.toSeq
  }

  def addModel(name: String, model: ModelInstance)(implicit ec: ExecutionContext): Future[Boolean] =
    modelRepository.addModel(name, model.toPmml)

}
