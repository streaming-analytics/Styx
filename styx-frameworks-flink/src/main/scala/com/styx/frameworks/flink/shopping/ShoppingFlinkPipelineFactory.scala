package com.styx.frameworks.flink.shopping

import java.util.concurrent.{Executors, TimeUnit}

import com.styx.interfaces.repository.{CriteriaFilterRepository, CustomerProfileRepository, NotificationFilterRepository}
import com.styx.common.Logging
import com.styx.domain.events.BusinessEvent._
import com.styx.domain.events.NotificationEvent._
import com.styx.domain.events.TransactionEvent._
import com.styx.domain.events.{BaseBusinessEvent, BaseNotificationEvent, BaseTransactionEvent}
import com.styx.frameworks.flink.{AsyncRepoGenders, CepWindowTrigger}
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object ShoppingFlinkPipelineFactory extends Logging {

  implicit val executionContext: ExecutionContextExecutor  = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

  def createCepPipeline(sourceStream: DataStream[BaseTransactionEvent],
                        repositoryCreate: => CustomerProfileRepository,
                        featureToggles: FeatureToggles,
                        asyncRepoGenders: AsyncRepoGenders): DataStream[BaseBusinessEvent] = {
    logger.info("Setting up CEP Engine!")

    // get parameters
    val minCountTrigger = repositoryCreate.getCepMinCountTrigger.getOrElse(0)
    val period = repositoryCreate.getCepPeriod.getOrElse(0 minutes)

    logger.info(s"Setting up CEP pipeline with min count trigger = $minCountTrigger and window period = $period minutes.")
    val trigger = new CepWindowTrigger[BaseTransactionEvent, GlobalWindow](period, minCountTrigger)

    val stream = sourceStream.name("CEP Engine source")
      .map(event => event.addTimeStamp("PRE_FIRST")).name("with ts")
      .filter(_.accNum != 0).name("No creditcard")
    stream.addSink(new RawEventToRepositorySink(repositoryCreate)).name("Transaction amount storage")

    val windowStream = stream
      .keyBy(_.cardId)
      .window(GlobalWindows.create()) // Assign everything to 1 window per key
      .trigger(trigger) // Trigger window purges/continues/fires based on frequency per last period, will return ALL items seen sofar
      .apply(new CepWindowResultFunction()).name("Pattern match")
      .map(window => window.copy(event = window.event.addTimeStamp("PRE_PATTERN"))).name("with ts")
    AsyncDataStream.unorderedWait(windowStream, new LoadResultingBalance(repositoryCreate), asyncRepoGenders.timeout.toMillis, TimeUnit.MILLISECONDS, asyncRepoGenders.capacity).name("DataLake queries")
      .map(new CepFunction(repositoryCreate, featureToggles)).flatMap(_.toSeq).name("CEP Engine")
      .map(event => event.addTimeStamp("PRE_LAST")).name("with ts")
  }

  def createCepFilterPipeline(sourceStream: DataStream[BaseBusinessEvent], profileRepositoryCreate: => CustomerProfileRepository, criteriaFilterRepositoryCreate: => CriteriaFilterRepository, asyncRepoGenders: AsyncRepoGenders): DataStream[BaseBusinessEvent] = {
    logger.info("Setting up Filter Engine!")
    val preppedSource = sourceStream.name("Styx source")
      .map(event => event.addTimeStamp("FIRST")).name("with ts")
    AsyncDataStream.unorderedWait(preppedSource, new LoadCriteriaFilters(profileRepositoryCreate, criteriaFilterRepositoryCreate), asyncRepoGenders.timeout.toMillis , TimeUnit.MILLISECONDS, asyncRepoGenders.capacity).name("DataLake queries")
      .map(new CriteriaFilterFunction()).name("Styx Filter Processor")
      .flatMap(_.toSeq).name("All criteria filters")
      .map(event => event.addTimeStamp("LAST")).name("with ts")
  }

  def createPipeline(sourceStream: DataStream[BaseBusinessEvent], profileRepositoryCreate: => CustomerProfileRepository, notificationFilterRepositoryCreate: => NotificationFilterRepository, asyncRepoGenders: AsyncRepoGenders): DataStream[BaseNotificationEvent] = {
    logger.info("Setting up Machine Learning Engine!")
    val preppedSource = sourceStream.name("Styx source")
      .map(event => event.addTimeStamp("FIRST")).name("with ts")
    AsyncDataStream.unorderedWait(preppedSource, new LoadNotificationFilters(profileRepositoryCreate, notificationFilterRepositoryCreate), asyncRepoGenders.timeout.toMillis , TimeUnit.MILLISECONDS, asyncRepoGenders.capacity).name("DataLake queries")
      .map(new MlFunction()).name("Styx ML")
      .flatMap(_.toSeq).name("All notifications")
      .map(event => event.addTimeStamp("LAST")).name("with ts")
  }
}
