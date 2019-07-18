package ai.styx.frameworks.flink

import java.util.concurrent.Executors

import ai.styx.common.Logging
import com.typesafe.config.Config
import org.apache.flink.api.common.JobID
import org.apache.flink.configuration.{ConfigConstants, Configuration, HighAvailabilityOptions, TaskManagerOptions}
import org.apache.flink.runtime.instance.{ActorGateway, AkkaActorGateway}
import org.apache.flink.runtime.jobgraph.{JobGraph, JobStatus}
import org.apache.flink.runtime.minicluster.{MiniCluster, MiniClusterConfiguration, RpcServiceSharing}
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.apache.flink.streaming.util.TestStreamEnvironment
import org.apache.flink.test.util.TestBaseUtils

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

trait EmbeddedFlink extends BeforeAndAfterAll {
  this: Suite =>

  def jobToBeDeployed: Any // StyxJob

  def jobName: String = this.getClass.getName

  def jobId: JobID = new JobID()

  def jobConfigPrefix: String

  def config: Config

  override def beforeAll(): Unit = {
    super.beforeAll()
    EmbeddedFlink.ensureStarted()
    EmbeddedFlink.deployJob(jobToBeDeployed, config, jobConfigPrefix, jobId)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    EmbeddedFlink.cluster.cancelJob(jobId) //.jobStatusGateway.cancelJob(jobName, 5 seconds)
    EmbeddedFlink.stopCluster()
  }
}

object EmbeddedFlink extends Logging {

  implicit val executionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(4))

  val numTaskManagers = 1
  val clusterParallelism = 8
  val defaultParallelismForJobs = 2

  val timeout: FiniteDuration = 1000 seconds  //TestBaseUtils.DEFAULT_TIMEOUT

  lazy val cluster: MiniCluster = startCluster()

  //def leaderGateway() = new AkkaActorGateway()

  //lazy val jobStatusGateway = new JobStatusGateway(leaderGateway())

  def startCluster(): MiniCluster = {
    val numTaskManagers = 1
    val startWebServer = true
    val startZooKeeper = false

    val config = new Configuration()

    config.setInteger(ConfigConstants.LOCAL_NUMBER_TASK_MANAGER, numTaskManagers)
    config.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, startWebServer)

    if (startZooKeeper) {
      config.setInteger(ConfigConstants.LOCAL_NUMBER_JOB_MANAGER, 3)
      config.setString(HighAvailabilityOptions.HA_MODE, "zookeeper")
    }

    val cluster = new MiniCluster(new MiniClusterConfiguration(config, numTaskManagers, RpcServiceSharing.DEDICATED, null))
    TestStreamEnvironment.setAsContext(cluster, defaultParallelismForJobs)
    cluster
  }

  def ensureStarted(): Unit = do Thread.sleep(100) while (!cluster.isRunning)

  def stopCluster(): Unit = {
    TestStreamEnvironment.unsetAsContext()
    cluster.closeAsync()
  }

  def deployJob(job: Any, config: Config, jobConfigPrefix: String, jobID: JobID): Unit = {
    Future {
      cluster.submitJob(new JobGraph())
      //job.run(config, Some(jobName))
    }
    val inputTopic: String = config.getString(jobConfigPrefix + ".read.rawDataTopic")
    val cepName: String = config.getString(jobConfigPrefix + ".name")
    waitUntilJobIsRunning(jobID)
  }

  def waitUntilJobIsRunning(jobID: JobID, pollingInterval: Duration = 500 milliseconds, timeout: FiniteDuration = timeout): Unit = {
    val startTime = System.currentTimeMillis()

    while ((cluster.getJobStatus(jobID).get != JobStatus.RUNNING) && System.currentTimeMillis() - startTime < timeout.toMillis) {
      Thread.sleep(pollingInterval.toMillis)
    }

    LOG.info(s"Job $jobID is deployed and running")
  }
}
