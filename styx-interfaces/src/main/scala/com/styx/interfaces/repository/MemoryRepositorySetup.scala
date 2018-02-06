package com.styx.interfaces.repository

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object MemoryRepositorySetup {

  implicit def ToRepositorySetup[T](customerProfileRepository: T) : RepositorySetup[T]=
    RepositorySetup(customerProfileRepository)

}

case class RepositorySetup[T](repository: T) extends AnyVal{

  def withSetup(closure: Seq[T=>Future[Boolean]])(implicit ec: ExecutionContext): T ={
    Await.ready(Future.sequence(closure.map(_(repository))).map(_.forall(identity)), 15 seconds)
    repository
  }
}

