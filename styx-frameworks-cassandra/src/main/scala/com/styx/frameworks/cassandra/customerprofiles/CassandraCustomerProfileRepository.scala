package com.styx.frameworks.cassandra.customerprofiles

import com.datastax.driver.core.Statement
import com.styx.common.Logging
import com.styx.common.LogFutureImplicit._
import com.styx.frameworks.cassandra.CqlAttemptorImplicit._
import com.styx.domain.{Balance, Customer}
import com.styx.interfaces.repository.CustomerProfileRepository
import com.styx.frameworks.cassandra.CassandraRepository
import org.joda.time.DateTime
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class CassandraCustomerProfileRepository(repo: CassandraRepository) extends CustomerProfileRepository with Logging {
  val statements = new CassandraCustomerProfileQueries(repo)

  /**
    * Get one customer profile from the database
    *
    * @param accountNumber The unique account number
    */
  def getCustomerProfile(accountNumber: Int)(implicit ec: ExecutionContext): Future[Option[Customer]] = {
    val statement: Statement = statements.bindGetCustomerProfile(accountNumber)
    repo.tryAsyncStatement(statement,
      "get_customer_profile",
      s"get customer profile for account $accountNumber")
      .map(_.all().asScala.map(statements.createCustomer))
      .logFailure(t => logger.error(s"FAILED to load customers for account number $accountNumber. Error message: ${t.getMessage}"))
      .logSuccess(customers =>
        if (customers.isEmpty)
          logger.warn(s"No customers found for account number $accountNumber.")
        else if (customers.lengthCompare(1) > 0)
          logger.warn(s"Too much customers found, expected 1, got ${customers.size}, taking the first.")
      ).map(_.headOption)
  }

  def getInitialBalance(cardId: String)(implicit ec: ExecutionContext): Future[Option[Double]] = {
    val statement: Statement = statements.bindGetInitialBalance(cardId)
    repo.tryAsyncStatement(statement,
      "get_initial_balance",
      s"get initial balance for card $cardId")
      .map(_.all().asScala.map(_.getDouble(0)))
      .logSuccess(initialBalances =>
        if (initialBalances.isEmpty)
          logger.warn(s"No initial balance found for card $cardId")
      ).map(_.headOption)
  }

  def getTransactionSum(accountNumber: Int, cardId: String, maxDateTime: DateTime)(implicit ec: ExecutionContext): Future[Double] = {
    val statement: Statement = statements.bindGetTransactionValues(cardId, maxDateTime)
    repo.tryAsyncStatement(statement,
      "get_transaction_sum",
      s"get transaction sum for account $accountNumber and card $cardId up till $maxDateTime")
      .map(_.all().asScala.map(_.getDouble(0)))
      .logSuccess(transactionSums =>
        logger.trace(s"${transactionSums.size} transactions found for for account number $accountNumber and card $cardId till $maxDateTime")
      ).map(_.sum)
  }

  def addTransaction(accountNumber: Int, cardId: String, dateTime: DateTime, amount: Double)(implicit ec: ExecutionContext): Future[Boolean] = {
    val statement: Statement = statements.bindAddTransaction(cardId, dateTime, amount)
    repo.tryAsyncStatement(statement,
      "add_transaction",
      s"add transaction for card $cardId at $dateTime")
      .logFailure(t => logger.error(s"FAILED to add transaction for account number $accountNumber and cardId: $cardId. Error message: ${t.getMessage}"))
      .map(_.wasApplied())
  }

  def insertProfile(customer: Customer)(implicit ec: ExecutionContext): Future[Boolean] = {
    val statement: Statement = statements.bindAddCustomerProfile(customer)
    repo.tryAsyncStatement(statement,
      "add_customer_profile",
      s"add customer profile for account ${customer.all("ACC_NUM")}").map(_.wasApplied())
  }

  def insertBalance(balance: Balance)(implicit ec: ExecutionContext): Future[Boolean] = {
    // TODO: Clean up to int
    val statement: Statement = statements.bindAddInitialBalance(balance.card_id, balance.acc_num, balance.balance, balance.card_type)
    //    accountNumber, Age, Gender, Count, AverageMonthlyExpenditures, Flag, Id,
    //      Address, Email, Gender)
    repo.tryAsyncStatement(statement,
      "add_initial_balance",
      s"add initial balance for Account ${balance.acc_num} with ${balance.card_type} Card ${balance.card_id}").map(_.wasApplied())
  }

  def getCepMinCountTrigger()(implicit ec: ExecutionContext): Option[Int] = {
    repo.tryStatement(statements.bindGetCepMinCountTrigger, "get_cep_min_count_trigger", "get cep min count trigger")
      .map(_.all().asScala.map(_.getInt(0)))
      .map(_.headOption)
      .getOrElse(None)
  }

  override def getCepPeriod()(implicit ec: ExecutionContext): Option[Duration] = {
    repo.tryStatement(statements.bindGetCepPeriod, "get_cep_period", "get cep period window")
      .map(_.all().asScala.map(_.getInt(0) minutes))
      .map(_.headOption)
      .getOrElse(None)
  }

  override def getCepBalanceThreshold()(implicit ec: ExecutionContext): Option[Int] = {
    repo.tryStatement(statements.bindGetCepBalanceThreshold, "get_cep_balance_threshold", "get cep balance threshold")
      .map(_.all().asScala.map(_.getInt(0)))
      .map(_.headOption)
      .getOrElse(None)
  }

  override def setCepParameters(periodInMinutes: Int, eventCount: Int, balanceThreshold: Int)(implicit ec: ExecutionContext): Future[Boolean] = ??? // TODO
}
