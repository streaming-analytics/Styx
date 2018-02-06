package com.styx.interfaces.repository

import com.styx.domain.{Balance, Customer}
import org.joda.time.DateTime

import scala.collection.parallel.mutable
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class MemoryCustomerProfileRepository extends CustomerProfileRepository {
  val customers = mutable.ParHashMap[Int, Customer]()
  val cardBalance = mutable.ParHashMap[String, Double]()
  val cardTransactions = mutable.ParHashMap[String, mutable.ParHashSet[(DateTime, Double)]]()
  val cepParameters = mutable.ParHashMap[String, Int]()

  def insertProfile(data: Customer)(implicit ec: ExecutionContext): Future[Boolean] = {
    Future {
      customers += (data.AccountNumber -> data)
      true
    }
  }

  def insertBalance(balance: Balance)(implicit ec: ExecutionContext): Future[Boolean] = {
    Future {
      cardTransactions += (balance.card_id -> mutable.ParHashSet[(DateTime, Double)]()) // there is no getOrElseUpdate on parallel mutable map, so initializing on initial balance
      cardBalance += (balance.card_id -> balance.balance)
      true
    }
  }

  def getCustomerProfile(accountNumber: Int)(implicit ec: ExecutionContext): Future[Option[Customer]] = {
    Future {
      customers.get(accountNumber)
    }
  }

  def getInitialBalance(cardId: String)(implicit ec: ExecutionContext): Future[Option[Double]] = {
    Future {
      cardBalance.get(cardId)
    }
  }

  def getTransactionSum(accountNumber: Int, cardId: String, maxDateTime: DateTime)(implicit ec: ExecutionContext): Future[Double] = {
    Future {
      cardTransactions.getOrElse(cardId, mutable.ParHashSet[(DateTime, Double)]()).filter(_._1.isBefore(maxDateTime)).map(_._2).sum
    }
  }

  def addTransaction(accountNumber: Int, cardId: String, dateTime: DateTime, amount: Double)(implicit ec: ExecutionContext): Future[Boolean] = {
    Future {
      cardTransactions.getOrElse(cardId, mutable.ParHashSet[(DateTime, Double)]()) += (dateTime -> amount)
      true
    }
  }

  def setCepParameters(periodInMinutes: Int, eventCount: Int, balanceThreshold: Int)(implicit ec: ExecutionContext) = {
    Future {
      cepParameters.put("trs_window", periodInMinutes)
      cepParameters.put("event_count", eventCount)
      cepParameters.put("balance_threshold", balanceThreshold)
      true
    }
  }

  def getCepPeriod()(implicit ec: ExecutionContext): Option[Duration] = Some(cepParameters("trs_window") minutes)
  def getCepMinCountTrigger()(implicit ec: ExecutionContext): Option[Int] = cepParameters.get("event_count")
  def getCepBalanceThreshold()(implicit ec: ExecutionContext): Option[Int] = cepParameters.get("balance_threshold")
}
