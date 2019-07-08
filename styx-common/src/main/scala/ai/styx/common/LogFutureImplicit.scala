package ai.styx.common

import scala.concurrent.{ExecutionContext, Future}

object LogFutureImplicit {

  implicit class LogFuture[A](val attempt: Future[A]) extends AnyVal {

    def logSuccess(logger: A => Unit)(implicit ec: ExecutionContext): Future[A] = {
      attempt.map(res => {
        logger(res)
        res
      })
    }

    def logFailure(logger: Throwable => Unit)(implicit ec: ExecutionContext): Future[A] = {
      attempt.transform(identity, e => {
        logger(e)
        e
      })
    }
  }

}
