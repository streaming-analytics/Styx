package com.styx.setup

import com.styx.common.BaseSpec
import com.styx.interfaces.repository._
import com.styx.domain._
import com.styx.domain.events.{BaseBusinessEvent, BaseNotificationEvent}
import com.styx.domain.models.ModelInstance
import com.styx.frameworks.flink.environment.SpecJobBuilder
import org.apache.flink.api.scala._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MlFunctionSpec extends BaseSpec with EventsFromSequence {

  def mockCustomer(accountNumber: Int) = Customer(
    Flag = "N",
    Id = "Y",
    Age = 50,
    Address = "Y",
    Email = "Y",
    Gender = "Y",
    Count = 1001,
    AverageMonthlyExpenditures = 3200,
    AccountNumber = accountNumber,
    all = Map(),
    "ME", "1970-01-01 01:01:01", "LO", "Y", "N")

  val globalAccNum = 1
  val globalCardId = "2"
  val globalEvent = "Shopping"

  def getJobFixture(constantScore: => Double,
                    threshold: Double,
                    withProfile: Boolean = true,
                    withNotifications: Boolean = true): ConfigBasedJobBuilderDefaultsFixture = {
    val accNum = globalAccNum
    val cardId = globalCardId
    val message = "just a message"
    val event = globalEvent
    val customer = mockCustomer(accNum)
    val setup = ConfigBasedJobBuilderDefaultsFixture()
      .withLocalEnv()
      .andProfileSetup(Seq(
        _.insertBalance(Balance(cardId, accNum, 50, "test")),
        r => if (withProfile) {
          r.insertProfile(customer)
        } else Future {
          true
        }
      ))
      .andNotificationFilterSetup(Seq(
        r => if (withNotifications) {
          r.addNotificationFilter(NotificationFilter("theName", event, new ModelInstance with Serializable{
            def toPmml: PmmlModel = null

            def score(customer: Customer): Double = constantScore
          }, threshold, message))
        } else Future {
          true
        }
      ))
    setup
  }

  def testNotificationGeneration(jobFixture: ConfigBasedJobBuilderDefaultsFixture): Option[List[BaseNotificationEvent]] = {
    val accNum = globalAccNum
    val cardId = globalCardId
    val event = globalEvent
    val jobSetup = jobFixture.createShoppingJobBuilder()
    val specJobSetup = SpecJobBuilder(jobFixture.streamEnvironment)
    val events = Seq(
      BaseBusinessEvent(DateTime.now(), accNum, cardId, event, Map("EVENT" -> event, "BALANCE" -> 50.00.asInstanceOf[Object]))
    )
    for (
      businessStream <- consumeEventsFromSequence(events, jobFixture.streamEnvironment);
      notificationStream <- jobSetup.businessEventsToNotificationEvents(businessStream);
      notificationsCollector <- specJobSetup.withEventAccumulator(notificationStream);
      result <- specJobSetup.execute()
    ) yield {
      notificationsCollector.allFrom(result)
    }
  }

  "The ML Engine" should "predict a relevancy score" in {
    val constantScore = 0.5
    val threshold = 0.4
    val jobFixture = getJobFixture(constantScore, threshold)

    val notification: Option[List[BaseNotificationEvent]] = testNotificationGeneration(jobFixture)
    withClue(notification){
      notification.get.size should be(1)
      val score = notification.get.head.payload.get("SCORE")
      score should not(be(None))
      score.get.toString.toDouble should be(constantScore)
    }
  }

  it should "not produce a notification if the model score is lower than the threshold" in {
    val constantScore = 0.5
    val threshold = 0.51
    val jobFixture = getJobFixture(constantScore, threshold)

    val notification = testNotificationGeneration(jobFixture)
    withClue(notification){
      notification.get.size should be(0)
    }
  }

  it should "not produce a notification if the model scoring fails with an exception " in {
    val threshold = 0.0
    val jobFixture = getJobFixture({throw new UnsupportedOperationException()}, threshold)

    val notification = testNotificationGeneration(jobFixture)
    withClue(notification){
      notification.get.size should be(0)
    }
  }

  it should "be resilient for missing profiles" in {
    val constantScore = 0.5
    val threshold = 0.4
    val jobFixture = getJobFixture(constantScore, threshold, withProfile = false)

    val notification = testNotificationGeneration(jobFixture)
    withClue(notification){
      notification.get.size should be(0)
    }
  }

  it should "be resilient for missing notification" in {
    val constantScore = 0.5
    val threshold = 0.4
    val jobFixture = getJobFixture(constantScore, threshold, withNotifications = false)

    val notification = testNotificationGeneration(jobFixture)
    withClue(notification){
      notification.get.size should be(0)
    }
  }

  def profileRepoCreate(): CustomerProfileRepository = {
    new MemoryCustomerProfileRepository()
  }

  def notificationRepoCreate(): NotificationFilterRepository = {
    new MemoryNotificationFilterRepository()
  }
}
