package com.styx.setup

import com.styx.interfaces.repository._
import com.typesafe.config.Config
import com.styx.domain.{Balance, CepDefinition, Customer, NotificationFilter}
import com.styx.domain.events.CriteriaFilter
import org.joda.time.DateTime

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object StubRepositoryFactory extends RepositoryFactory {

  override def createRepositoryInstances(styxConfig: Config): RepositoryInstances = {
    RepositoryInstances(
      stubCustomerProfileRepository,
      stubCriteriaFilterRepository,
      stubNotificationFilterRepository,
      stubCepDefinitionRepository)
  }

  override def createCustomerProfileRepository(styxConfig: Config): CustomerProfileRepository =  stubCustomerProfileRepository

  override def createNotificationFilterRepository(styxConfig: Config): NotificationFilterRepository = stubNotificationFilterRepository

  override def createCriteriaFilterRepository(styxConfig: Config): CriteriaFilterRepository = stubCriteriaFilterRepository

  def stubNotificationFilterRepository: NotificationFilterRepository = {
    new NotificationFilterRepository {
      override def getNotificationFilters(event: String)(implicit ec: ExecutionContext): Future[Seq[NotificationFilter]] = Future { Seq() }
      override def addNotificationFilter(filter: NotificationFilter)(implicit ec: ExecutionContext): Future[Boolean] = Future { true }
    }
  }

  def stubCriteriaFilterRepository: CriteriaFilterRepository = {
    new CriteriaFilterRepository {
      override def getCriteriaFilters(event: String)(implicit ec: ExecutionContext): Future[Seq[CriteriaFilter]] = Future { Seq(CriteriaFilter("ALWAYS_TRUE", "Shopping", _ => Right(true))) }

      override def addCriteriaFilter(filter: CriteriaFilter)(implicit ec: ExecutionContext): Future[Boolean] = Future { true }
    }
  }

  def stubCepDefinitionRepository: CepDefinitionRepository = {
    new CepDefinitionRepository {
      override def getCepDefinitions(event: String): Seq[CepDefinition] = Seq()
    }
  }

  def stubCustomerProfileRepository: CustomerProfileRepository = {
    new CustomerProfileRepository {
      override def getInitialBalance(cardId: String)(implicit ec: ExecutionContext): Future[Option[Double]] = Future { Some(100.0) }
      override def insertBalance(balance: Balance)(implicit ec: ExecutionContext): Future[Boolean] = Future { true }
      override def getTransactionSum(accountNumber: Int, cardId: String, maxDateTime: DateTime)(implicit ec: ExecutionContext): Future[Double] = Future { 1000.0 }
      override def getCustomerProfile(accountNumber: Int)(implicit ec: ExecutionContext): Future[Option[Customer]] = Future { Some(
        Customer(
          Flag = "a",
          Id = "b",
          Age = 30,
          Address = "c",
          Email = "d",
          Gender = "e",
          Count = 10,
          AverageMonthlyExpenditures = 0,
          AccountNumber = 102,
          all = Map.empty,
          Segment = "high",
          ContactDate = "f",
          Rating = "g",
          CreditCard = "h",
          Budget = "i"
        )
      ) }
      override def addTransaction(accountNumber: Int, cardId: String, dateTime: DateTime, amount: Double)(implicit ec: ExecutionContext): Future[Boolean] = Future { true }
      override def insertProfile(customer: Customer)(implicit ec: ExecutionContext): Future[Boolean] = Future { true }
      override def getCepPeriod()(implicit ec: ExecutionContext): Option[Duration] = Some(75 minutes)
      override def getCepMinCountTrigger()(implicit ec: ExecutionContext): Option[Int] = Some(5)
      override def getCepBalanceThreshold()(implicit ec: ExecutionContext): Option[Int] = Some(200)
      override def setCepParameters(periodInMinutes: Int, eventCount: Int, balanceThreshold: Int)(implicit ec: ExecutionContext): Future[Boolean] = Future { true }
    }
  }
}
