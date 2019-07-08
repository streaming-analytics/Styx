package ai.styx.common

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

object RetryHelper extends Logging {
  /**
    * Retries the command at most retries time when ReadTimeoutExceptions occur.
    * After retries times rethrows the exception
    *
    * @param command the operation to try
    * @param retries the max number of times to retry the operation
    * @tparam T the resulttype
    * @return the result of the succesfull execution of the command
    */
  def retryExecute[T](retries: Int = 0, sleepTime: Int = 1000)(command: => T): Try[T] = {
    Try {
      command
    } recoverWith {
      case e: Throwable =>
        if (retries == 0) {
          LOG.warn(s"Got exception, $retries retries left, so throwing exception ${e.getMessage}.")
          Failure(e)
        } else {
          LOG.warn(s"Got exception, $retries retries left, so sleeping $sleepTime: ${e.getMessage}.")
          Thread.sleep(sleepTime)
          retryExecute(retries - 1, sleepTime)(command)
        }
    }
  }

  /**
    * Retries the command at most retries time when ReadTimeoutExceptions occur.
    * After retries times rethrows the exception
    *
    * @param command the operation to try
    * @param retries the max number of times to retry the operation
    * @tparam T the resulttype
    * @return the result of the succesfull execution of the command
    */
  def retryExecuteFuture[T](retries: Int = 0, sleepTime: Int = 1000)(command: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    command.recoverWith {
      case e: Throwable =>
        if (retries == 0) {
          LOG.warn(s"Got exception, $retries retries left, so throwing exception ${e.getMessage}.")
          Future.failed(e)
        } else {
          LOG.warn(s"Got exception, $retries retries left, so sleeping $sleepTime: ${e.getMessage}.")
          Thread.sleep(sleepTime)
          retryExecuteFuture(retries - 1, sleepTime)(command)
        }
    }
  }
}
