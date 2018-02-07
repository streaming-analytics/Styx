package com.styx.runner

import com.styx.common.RetryHelper._
import com.styx.common.UsingImplicit._
import com.styx.common.{ConfigUtils, FileReader, Logging}
import com.styx.domain.{Balance, Customer}
import com.styx.frameworks.cassandra.CqlBatchExecutor
import com.styx.frameworks.cassandra.customerprofiles.{CassandraCustomerProfileRepository, CustomerProfileSerializer}
import com.styx.setup.CassandraRepositoryFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object CustomerProfileReader extends Logging {

  def futureToFutureTry[T](f: Future[T]): Future[Try[T]] = f.map(Success(_)).recover({ case x => Failure(x) })

  def main(args: Array[String]): Unit = {
    val predefinedArgs = Array[String]()
    val config = ConfigUtils.loadConfig(predefinedArgs ++ args)

    val repo = CassandraRepositoryFactory.createCassandraRepository(config)
    val profileRepository = new CassandraCustomerProfileRepository(repo)
    val profileFuture = Future.fromTry(usingTry(FileReader.loadResource("/customer_profiles_2017-01-04_13-52-55.csv")) { resource => {
      val profiles = resource.getLines().drop(1).map(readProfile)
        val result = new CqlBatchExecutor().runBatch(profiles, profileRepository.insertProfile _, 1000)
      Await.ready(result, Duration.Inf)
    }
    }).flatMap(identity)

    var balanceAttemptCount = 0
    var balanceSucceededCount = 0
    var balanceFailedCount = 0
    val balanceFuture = Future.fromTry(usingTry(FileReader.loadResource("/initial-balance-data-set-2017-01-02-V2.csv")) { resource => {
      val result = resource.getLines().grouped(50).map(
        lines => {
          val balances = lines.map(readBalance)
          val futuresBatch = balances.map(p => retryExecuteFuture(10)(profileRepository.insertBalance(p)))
          balanceAttemptCount += futuresBatch.size
          val resultFuture = Future.sequence(futuresBatch)
          val resultAttempts = Future.sequence(futuresBatch.map(futureToFutureTry)).map(_.collect({
            case Success(x) => x
            case Failure(_) => false
          }))
          balanceSucceededCount += Await.result(resultAttempts.map(_.count(identity)), Duration.Inf)
          balanceFailedCount += Await.result(resultAttempts.map(_.count(! _)), Duration.Inf)
          Await.ready(resultFuture, Duration.Inf)
        })
      Await.ready(Future.sequence(result.toList), Duration.Inf)
    }
    }).flatMap(identity)

    Console.println("Done firing queries")
    Await.result(profileFuture, Duration.Inf)
    Console.println("Done with profiles")
    Await.result(balanceFuture, Duration.Inf)
    logger.warn(s"Finished with balances, out of $balanceAttemptCount  there were $balanceSucceededCount successes and $balanceFailedCount failures.")
    Console.println("Done with balances")
  }

  def readBalance(line: String): Balance = {
    val columns = line.split(",")
    // cardid,account_number,initial_balance,card_type
    Balance(
      columns(0),
      columns(1).toInt,
      columns(2).toDouble,
      columns(3)
    )
  }

  def readProfile(line: String): Customer = {
    val columns = line.split(";")
    new CustomerProfileSerializer().fromStringList(columns.toList)
  }

}
