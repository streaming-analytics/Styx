package com.styx.runner

import java.util.concurrent.Executors

import com.styx.StyxJob
import com.styx.common.Logging
import com.typesafe.config.Config
import org.apache.flink.runtime.instance.ActorGateway
import org.apache.flink.runtime.minicluster.LocalFlinkMiniCluster
import org.apache.flink.streaming.util.TestStreamEnvironment
import org.apache.flink.test.util.TestBaseUtils
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

trait EmbeddedFlink extends BeforeAndAfterAll {
  this: Suite =>

  def jobToBeDeployed: StyxJob

  def jobName: String = this.getClass.getName

  def jobConfigPrefix: String

  def config: Config

  override def beforeAll(): Unit = {
    super.beforeAll()
    EmbeddedFlink.ensureStarted()
    EmbeddedFlink.deployJob(jobToBeDeployed, config, jobConfigPrefix, jobName)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    EmbeddedFlink.jobStatusGateway.cancelJob(jobName, 5 seconds)
    EmbeddedFlink.stopCluster()
  }
}

object EmbeddedFlink extends Logging {

  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  val clusterParallelism = 8
  val defaultParallelismForJobs = 2

  val timeout: FiniteDuration = 1000 seconds  //TestBaseUtils.DEFAULT_TIMEOUT

  lazy val cluster: LocalFlinkMiniCluster = startCluster()

  lazy val jobStatusGateway = new JobStatusGateway(leaderGateway())

  def startCluster(): LocalFlinkMiniCluster = {
    val numOfTaskManagers = 1
    val startWebServer = true
    val startZooKeeper = false
    val singleActorSystem = true
    val cluster = TestBaseUtils.startCluster(numOfTaskManagers, clusterParallelism, startWebServer, startZooKeeper, singleActorSystem)
    TestStreamEnvironment.setAsContext(cluster, defaultParallelismForJobs)
    cluster
  }

  def ensureStarted(): Unit = leaderGateway() // just do anything that requires reference to cluster, so it is initialized

  private def leaderGateway(): ActorGateway = cluster.getLeaderGateway(timeout)

  def stopCluster(): Unit = {
    TestStreamEnvironment.unsetAsContext()
    TestBaseUtils.stopCluster(cluster, timeout)
  }

  def deployJob(job: StyxJob, config: Config, jobConfigPrefix: String, jobName: String): Unit = {
    Future {
      job.run(config, Some(jobName))
    }
    val inputTopic: String = config.getString(jobConfigPrefix + ".read.topic")
    val cepName: String = config.getString(jobConfigPrefix + ".name")
    waitUntilJobIsRunning(jobName, cepName, inputTopic)
  }

  def waitUntilJobIsRunning(jobName: String, cepName: String, inputTopic: String, pollingInterval: Duration = 500 milliseconds, timeout: FiniteDuration = timeout): Unit = {
    val startTime = System.currentTimeMillis()
    while (!jobStatusGateway.isJobRunning(jobName, cepName, inputTopic, timeout) && System.currentTimeMillis() - startTime < timeout.toMillis) {
      Thread.sleep(pollingInterval.toMillis)
    }
    if (!jobStatusGateway.isJobRunning(jobName, cepName, inputTopic, timeout)) {
      val jobNames: Iterable[String] = jobStatusGateway.getJobStatuses(timeout).map(_.getJobName)
      if (jobNames.toList.contains(jobName)) {
        throw new RuntimeException("Job is running on cluster, but not ready - most probably kafka consumer offsets were not initialized on time")
      } else if ((jobNames.size + 1) * defaultParallelismForJobs > clusterParallelism) {
        throw new RuntimeException(s"Could not deploy job $jobName. Cluster parallelism exceeded.")
      } else {
        throw new RuntimeException(s"There was no job with name $jobName running, the only running jobs were: ${jobNames.mkString(",")}")
      }
    } else {
      logger.info(s"Job $jobName is deployed and running")
    }
  }
}
