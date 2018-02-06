package com.styx.common

object LogOptionImplicit {

  implicit class LogOption[A](val attempt: Option[A]) extends AnyVal {

    def logSome(logger: A => Unit): Option[A] = {
      attempt.map(res => {
        logger(res)
        res
      })
    }

    def logNone(logger:  => Unit): Option[A] = {
      if (attempt.isEmpty) {
        logger
      }
      attempt
    }
  }

}
