package com.styx.setup

import com.styx.common.BaseSpec
import com.styx.interfaces.repository.{CustomerProfileRepository, MemoryCustomerProfileRepository}
import com.styx.domain.Balance
import com.styx.frameworks.flink.domain.TransactionEventFactory
import com.styx.frameworks.flink.environment.SpecJobBuilder
import org.apache.flink.api.scala._
import org.joda.time.{DateTime, Period}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class CepPipelineSpec extends BaseSpec with EventsFromSequence {

  val initialBalance = 200

  def getJobFixture: ConfigBasedJobBuilderDefaultsFixture = {
    val localInitialBalance = 200
    val setup = ConfigBasedJobBuilderDefaultsFixture()
      .withLocalEnv()
      .withProfileRepo({
        val profiles = new MemoryCustomerProfileRepository()
        profiles.insertBalance(Balance("1", 1, localInitialBalance, "test"))
        profiles.setCepParameters(60, 5, 200)
        profiles // starts with amount that is low enough to trigger cep
      })
      .andProfileSetup(Seq(_.insertBalance(Balance("2", 2, localInitialBalance, "test"))))
    setup
  }

  "the full cep pipeline" should "trigger on window" in {
    val jobFixture = getJobFixture
    val jobSetup = jobFixture.createShoppingJobBuilder()
    val specJobSetup = SpecJobBuilder(jobFixture.streamEnvironment)
    val transactionEvents = TransactionEventFactory.examplePattern()
    for (
      transactions <- consumeEventsFromSequence(transactionEvents, jobFixture.streamEnvironment);
      businessStream <- jobSetup.transactionsToBusinessEvents(transactions);
      businessCollector <- specJobSetup.withEventAccumulator(businessStream);
      result <- specJobSetup.execute()
    ) {
      val businessEvents = businessCollector.allFrom(result)
      withClue(businessEvents) {
        businessEvents.size should be(1)
        businessEvents.head.payload.get("EVENT_CD") should be(Some("Shopping"))
        businessEvents.head.payload.get("BALANCE") should be(Some(150D.toString))
        businessEvents.head.payload.get("ACC_NUM") should be(Some(transactionEvents.head.accNum.toString))
        businessEvents.head.payload.get("CARD_ID") should be(Some(transactionEvents.head.cardId))
        businessEvents.head.payload.get("TRACE_ID") should not(be(None))
        businessEvents.head.payload.get("TIMESTAMPS") should not(be(None))
        businessEvents.head.payload.get("EVENT_DTTM").map(value => DateTime.parse(value.toString).toString) should be(Some(
          businessEvents.head.eventTime.toString
        ))
      }
    }
  }

  it should "trigger twice with 1 extra message" in {
    val jobFixture = getJobFixture
    val jobSetup = jobFixture.createShoppingJobBuilder()
    val specJobSetup = SpecJobBuilder(jobFixture.streamEnvironment)
    val numRaw = TransactionEventFactory.minimalCount + 1
    val transactionEvents = TransactionEventFactory.examplePattern(length = numRaw)
    for (
      transactions <- consumeEventsFromSequence(transactionEvents, jobFixture.streamEnvironment);
      businessStream <- jobSetup.transactionsToBusinessEvents(transactions);
      businessCollector <- specJobSetup.withEventAccumulator(businessStream);
      result <- specJobSetup.execute()
    ) {
      val businessEvents = businessCollector.allFrom(result)
      withClue(businessEvents) {
        businessEvents.size should be(2)
      }
    }
  }

  it should "unravel multiple patterns" in {
    val jobFixture = getJobFixture
    val cardIds = (0 to 5).map(_.toString)
    val balanceInsertions =
      cardIds.map(cardId => {
        profiles: CustomerProfileRepository => profiles.insertBalance(Balance(cardId, 1, 200, "test"))
      })
    val setup: ConfigBasedJobBuilderDefaultsFixture = jobFixture.andProfileSetup(balanceInsertions)
    val jobSetup = setup.createShoppingJobBuilder()
    val specJobSetup = SpecJobBuilder(jobFixture.streamEnvironment)
    val numRaw = TransactionEventFactory.minimalCount
    val patternForCardWithId = {cardId: String=>TransactionEventFactory.examplePattern(
          cardId = cardId,
          length = numRaw,
          step = Period.millis(2000 + Random.nextInt(1000)))}
    val transactionEvents = cardIds
      .par
      .flatMap(patternForCardWithId)
      .toList // I expected I needed to sort them , but also works withou ...: .sortBy(_.eventTime.getMillis) // should enter the system ordered by time
    transactionEvents.foreach(event => logger.info(s"Prepared sequence contains $event"))
    for (
      transactions <- consumeEventsFromSequence(transactionEvents, jobFixture.streamEnvironment);
      businessStream <- jobSetup.transactionsToBusinessEvents(transactions);
      businessCollector <- specJobSetup.withEventAccumulator(businessStream);
      result <- specJobSetup.execute()
    ) {
      val businessEvents = businessCollector.allFrom(result)
      withClue(businessEvents) {
        businessEvents.size should be(cardIds.size)
        businessEvents.map(_.cardId) should contain allElementsOf cardIds
      }
    }
  }
}
