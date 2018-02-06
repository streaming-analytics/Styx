package com.styx.interfaces.repository

import com.styx.domain.{Balance, Customer}
import org.joda.time.DateTime

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

trait CustomerProfileRepository {

  def insertProfile(customer: Customer)(implicit ec: ExecutionContext): Future[Boolean]

  def getCustomerProfile(accountNumber: Int)(implicit ec: ExecutionContext): Future[Option[Customer]]

  def getInitialBalance(cardId: String)(implicit ec: ExecutionContext): Future[Option[Double]]

  def insertBalance(balance: Balance)(implicit ec: ExecutionContext): Future[Boolean]

  /**
    * Finds the sum of all transaction amounts
    * @param accountNumber
    * @param cardId
    * @param maxDateTime the latest time, is an EXCLUSIVE Gender, thus only transactions with timestamp strictly less
    * @return
    */
  def getTransactionSum(accountNumber: Int, cardId: String, maxDateTime: DateTime)(implicit ec: ExecutionContext): Future[Double]

  def addTransaction(accountNumber: Int, cardId: String, dateTime: DateTime, amount: Double)(implicit ec: ExecutionContext): Future[Boolean]

  def setCepParameters(periodInMinutes: Int, eventCount: Int, balanceThreshold: Int)(implicit ec: ExecutionContext): Future[Boolean]
  def getCepPeriod()(implicit ec: ExecutionContext): Option[Duration]
  def getCepMinCountTrigger()(implicit ec: ExecutionContext): Option[Int]
  def getCepBalanceThreshold()(implicit ec: ExecutionContext): Option[Int]
}

