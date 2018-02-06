package com.styx

import com.styx.common.Logging
import org.apache.flink.runtime.client.JobStatusMessage
import org.apache.flink.runtime.instance.ActorGateway
import org.apache.flink.runtime.jobgraph.JobStatus
import org.apache.flink.runtime.messages.JobManagerMessages
import org.apache.flink.runtime.messages.JobManagerMessages.{CancelJob, RunningJobsStatus}

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

class JobStatusGateway(leaderGateway: ActorGateway, flinkWebAddress: String = "localhost:8081") extends Logging {

  def verifyKafkaOffsetInitialized(jobId: String, cepName: String, topic: String): Boolean = {
    new FlinkKafkaConsumerOffsetChecker(flinkWebAddress).isInitializedForJob(jobId, cepName, topic)
  }

  def isJobRunning(jobName: String, cepName: String, inputTopic: String, timeout: FiniteDuration): Boolean = {
    val jobStatuses: Iterable[JobStatusMessage] = getJobStatuses(timeout)
    val jobStatusMessage = jobStatuses.find(message => message.getJobName == jobName && message.getJobState == JobStatus.RUNNING)
    jobStatusMessage.exists {
      message => verifyKafkaOffsetInitialized(message.getJobId.toString, cepName, inputTopic)
    }
  }

  def getJobStatuses(timeout: FiniteDuration): Iterable[JobStatusMessage] = {
    val future = leaderGateway.ask(JobManagerMessages.RequestRunningJobsStatus, timeout)
      .mapTo[RunningJobsStatus]
    val jobsStatus = Await.result(future, timeout).runningJobs
    jobsStatus
  }

  def cancelJob(jobName: String, timeout: FiniteDuration): Unit = {
    val jobID = getJobStatuses(timeout).find(_.getJobName == jobName).get.getJobId

    logger.info(s"Waiting for job cancellation: $jobName")
    val cancelJobFuture = leaderGateway.ask(CancelJob(jobID), timeout)
    Await.result(cancelJobFuture, timeout)
    logger.info(s"Cancellation for job $jobName completed")
  }
}
