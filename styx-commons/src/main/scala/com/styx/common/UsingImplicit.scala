package com.styx.common

import java.io.IOException

import com.styx.common.LogTryImplicit._

import scala.util.{Failure, Try}

object UsingImplicit extends Logging {

  def using[A <: {def close() : Unit}, B](resourceCreate: => A)(resourceHandle: A => Try[B]): Try[B] = {
    val resource = Try {
      resourceCreate
    }.map(Option(_))
    try {
      resource.flatMap {
        case None =>
          Failure(new IOException("Resource 'null' returned, cannot call function"))
        case Some(r) => resourceHandle(r)
      }
    } finally {
      resource.map(r => r.map(someResource => Try {
        someResource.close()
      }.logFailure(e => logger.error("Failed to close resource", e))))
    }
  }

  def usingTry[A <: {def close() : Unit}, B](param: => A)(f: A => B): Try[B] = {
    using {
      param
    }(r => Try {
      f(r)
    })
  }

}
