package com.styx.frameworks.cassandra

// Because the used cassandra driver is shaded, we have to convert the shaded classes,...or not
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}

import scala.concurrent.{Future, Promise}

object FutureImplicit {

  implicit class RichListenableFuture[T](lf: ListenableFuture[T]) {
    def asScala: Future[T] = {
      val p = Promise[T]()
      Futures.addCallback(lf, new FutureCallback[T] {
        def onFailure(t: Throwable): Unit = p failure t
        def onSuccess(result: T): Unit    = p success result
      })
      p.future
    }
  }
}
