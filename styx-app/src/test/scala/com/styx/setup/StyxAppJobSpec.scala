package com.styx.setup

import com.styx.common.BaseSpec
import com.styx.interfaces.repository.MemoryCustomerProfileRepository
import com.styx.domain.Balance
import com.styx.frameworks.flink.domain.TransactionEventFactory
import com.styx.frameworks.flink.environment.SpecJobBuilder
import org.apache.flink.api.scala._

import scala.concurrent.ExecutionContext.Implicits.global

class StyxAppJobSpec extends BaseSpec with EventsFromSequence{

  "The full styx pipeline with stubbed kafka" should "be able to start" in {
    val jobFixture = ConfigBasedJobBuilderDefaultsFixture()
      .withLocalEnv()
      .withProfileRepo({
        val profiles = new MemoryCustomerProfileRepository()
        profiles.insertBalance(Balance("1", 1, 200, "test"))
        profiles.setCepParameters(60, 5, 200)
        profiles
      })
      .andProfileSetup(Seq(_.insertBalance(Balance("2", 2, 200, "test"))))  //, _.setCepParameters(60, 5, 200)))
    val jobSetup = jobFixture.createShoppingJobBuilder()
    val specJobSetup = SpecJobBuilder(jobFixture.streamEnvironment)
    for (
      transactions <- consumeEventsFromSequence(TransactionEventFactory.examplePattern(),
                                     jobFixture.streamEnvironment);

      businessStream <- jobSetup.transactionsToBusinessEvents(transactions);
      businessCollector <- specJobSetup.withEventAccumulator(businessStream);
      //notificationStream <- setup.job.businessEventsToNotificationEvents(businessStream);
      //ccStream <- setup.job.notificationEventsToCcEvents(notificationStream);
      //ccCollector <- setup.withEventAccumulator(ccStream);
      result <- specJobSetup.execute()
    ) {
      val businessEvents = businessCollector.allFrom(result)
      withClue(businessEvents) {
        businessEvents.size should be(1)
      }
    }
  }
}

