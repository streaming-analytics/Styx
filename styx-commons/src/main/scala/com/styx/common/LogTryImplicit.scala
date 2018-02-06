package com.styx.common

import scala.util.{Failure, Try}

object LogTryImplicit {

  implicit class LogTry[A](val attempt: Try[A]) extends AnyVal{

    def logSuccess(logger: A => Unit): Try[A] = {
      attempt.map(res => {
        logger(res)
        res
      })
    }

    def logFailure(logger: Throwable => Unit): Try[A] = {
      attempt recoverWith {
        case e: Throwable =>
          logger(e)
          Failure(e)
      }
    }
  }
}
