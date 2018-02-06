package com.styx.frameworks.flink.shopping

import com.styx.interfaces.repository.CustomerProfileRepository
import com.styx.common.LogFutureImplicit._
import com.styx.domain.events.BaseTransactionEvent
import com.styx.common.Logging
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}

import scala.concurrent.ExecutionContext.Implicits.global

case class ResultingBalance(value: Double)

class LoadResultingBalance(repositoryCreate: => CustomerProfileRepository) extends AsyncFunction[TransactionWindow, (ResultingBalance, BaseTransactionEvent)] with Logging with Serializable {

  // RepositoryInstances are not serializable, thus
  @transient private lazy val repositoryInstance = repositoryCreate

  override def asyncInvoke(window: TransactionWindow, resultFuture: ResultFuture[(ResultingBalance, BaseTransactionEvent)]): Unit = {
    val re = window.event

    // Trigger futures in parallel
    def initialBalanceQuery = repositoryInstance.getInitialBalance(re.cardId)
    def transactionSumQuery = repositoryInstance.getTransactionSum(re.accNum, re.cardId, window.start) // Get total of transactions before window

    // if any fails (at any moment, immediately collect empty result set such that flink can continue
    initialBalanceQuery.onFailure { case _ => resultFuture.complete(Iterable()) }
    transactionSumQuery.onFailure { case _ => resultFuture.complete(Iterable()) }

    // Combine both futures to 1 single result
    val dataFuture = for (initialBalanceOpt <- initialBalanceQuery;
                          transactionSum <- transactionSumQuery)
      yield (initialBalanceOpt, transactionSum)
    // Log if any of the futures fails
    dataFuture.logFailure(e => logger.error("Failed to determine BusinessEvent.", e))

    // When both futures succeed, forward the result
    dataFuture.onSuccess {
      case (None, _) => resultFuture.complete(Iterable())
      case (Some(initialBalance), transactionSum)=>
        val balance = initialBalance - transactionSum - window.transactionSum
        logger.trace("Found customer, current balance is: " + balance)
        resultFuture.complete(Iterable((ResultingBalance(balance), re)))
    }
  }
}
