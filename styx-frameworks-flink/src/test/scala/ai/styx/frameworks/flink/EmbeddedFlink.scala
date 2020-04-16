package ai.styx.frameworks.flink

import java.util.concurrent.Executors

import ai.styx.common.Logging
import com.typesafe.config.Config
import org.apache.flink.api.common.{JobID, JobStatus}
import org.apache.flink.configuration.{ConfigConstants, Configuration, HighAvailabilityOptions}
import org.apache.flink.runtime.jobgraph.JobGraph
import org.apache.flink.runtime.messages.Acknowledge
import org.apache.flink.runtime.minicluster.{MiniCluster, MiniClusterConfiguration, RpcServiceSharing}
import org.scalatest.{BeforeAndAfterAll, Suite}
import org.apache.flink.streaming.util.TestStreamEnvironment

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

trait EmbeddedFlink extends BeforeAndAfterAll {
  this: Suite =>

  def jobToBeDeployed: JobGraph

  def jobName: String = this.getClass.getName

  def jobConfigPrefix: String

  def config: Config

  override def beforeAll(): Unit = {
    super.beforeAll()
    EmbeddedFlink.ensureStarted()
    EmbeddedFlink.deployJob(jobToBeDeployed: JobGraph, config, jobConfigPrefix)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    EmbeddedFlink.cluster.cancelJob(jobToBeDeployed.getJobID)
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

  def deployJob(job: JobGraph, config: Config, jobConfigPrefix: String): Unit = {
    Future {
      cluster.submitJob(job)
    }
    val inputTopic: String = config.getString(jobConfigPrefix + ".read.rawDataTopic")
    val cepName: String = config.getString(jobConfigPrefix + ".name")
    waitUntilJobIsRunning(job.getJobID)
  }

  def waitUntilJobIsRunning(jobID: JobID, pollingInterval: Duration = 500 milliseconds, timeout: FiniteDuration = timeout): Unit = {
    val startTime = System.currentTimeMillis()

    while ((cluster.getJobStatus(jobID).get != JobStatus.RUNNING) && System.currentTimeMillis() - startTime < timeout.toMillis) {
      Thread.sleep(pollingInterval.toMillis)
    }

    LOG.info(s"Job $jobID is deployed and running")
  }

  def verifyKafkaOffsetInitialized(jobId: String, cepName: String, topic: String, flinkWebAddress: String = "localhost:8081"): Boolean = {
    new FlinkKafkaConsumerOffsetChecker(flinkWebAddress).isInitializedForJob(jobId, cepName, topic)
  }

  def isJobRunning(jobID: JobID, cepName: String, inputTopic: String, timeout: FiniteDuration): Boolean = {
    if (getJobStatus(jobID) == JobStatus.RUNNING) {
      verifyKafkaOffsetInitialized(jobID.toString, cepName, inputTopic)
    } else false
  }

  def getJobStatus(jobID: JobID): JobStatus = {
    cluster.getJobStatus(jobID).get()
  }

  def cancelJob(jobID: JobID, timeout: FiniteDuration): Unit = {
    LOG.info(s"Waiting for job cancellation: ${jobID.toString}")
    val cancelJobFuture = this.cluster.cancelJob(jobID)
    cancelJobFuture.complete(Acknowledge.get())
    LOG.info(s"Cancellation for job ${jobID.toString} completed")
  }
}
