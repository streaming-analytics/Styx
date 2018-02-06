package com.styx.setup

import com.styx.common.BaseSpec
import com.styx.shopping.{ConfigBasedDatagenJobBuilder, ConfigBasedJobBuilderDefaults, ConfigBasedShoppingJobBuilder}

/**
  * Verifies if the job can be constructed.
  *
  * It is a 'test' (unit test) because it does not actually talks to external systesm, it only constructs them
  * It is an 'test', and not a standalone app,
  *   - because it does not talk to a deployed styx,
  *   - but instead uses the code of this and dependent modules,
  */
class ConfigBasedJobBuilderSpec extends BaseSpec {

  def jobFactory(): ConfigBasedShoppingJobBuilder = {
    ConfigBasedJobBuilderDefaults.shoppingJobWithDefaultsGivenConfig()
  }

  def datagenFactory(): ConfigBasedDatagenJobBuilder = {
    ConfigBasedJobBuilderDefaults.datagenJobWithDefaultsGivenConfig()
  }

  "The ConfigBasedJobBuilder" should "create consumer of raw events from kafka" in {
    val jf = jobFactory()
    val result = for (events <- jf.readTransactionsFromKafka()) yield events
    result should not(be(None))
  }

  it should "create consumer of raw events from datafile" in {
    val jf = datagenFactory()
    val result = for (events <- jf.readTransactionsFromDatafile()) yield events
    result should not(be(None))
  }

  it should "create consumer of business events from kafka" in {
    val jf = jobFactory()
    val result = for (events <- jf.readBusinessEventsFromKafka()) yield events
    result should not(be(None))
  }

  it should "create consumer of notification events from kafka" in {
    val jf = jobFactory()
    val result = for (events <- jf.readNotificationEventsFromKafka()) yield events
    result should not(be(None))
  }

  it should "create consumer of random business events" in {
    val jf = datagenFactory()
    val result = for (events <- jf.randomBusinessEvents()) yield events
    result should not(be(None))
  }

  it should "create writer of business events to kafka" in {
    val jf = datagenFactory()
    val result = for (
      events <- jf.randomBusinessEvents();
      sink <- jf.businessEventsToKafka(events)) yield sink
    result should not(be(None))
  }

  it should "create writer of notification events to kafka" in {
    val jf = jobFactory()
    val result = for (
      events <- jf.readNotificationEventsFromKafka();
      sink <- jf.notificationEventsToKafka(events)) yield sink
    result should not(be(None))
  }

  it should "create writer of cc events to kafka" in {
    val jf = jobFactory()
    val result = for (
      events <- jf.readNotificationEventsFromKafka();
      cc <- jf.notificationEventsToCcEvents(events);
      sink <- jf.ccEventsToKafka(cc)) yield sink
    result should not(be(None))
  }

  it should "create transformer of transaction events to business events" in {
    val jf = datagenFactory()
    val result = for (
      sourceEvents <- jf.randomTransactions();
      resultEvents <- jf.transactionsToBusinessEvents(sourceEvents)) yield resultEvents
    result should not(be(None))
  }

  it should "create transformer of business events to notification events" in {
    val jf = datagenFactory()
    val result = for (
      sourceEvents <- jf.randomBusinessEvents();
      resultEvents <- jf.businessEventsToNotificationEvents(sourceEvents)) yield resultEvents
    result should not(be(None))
  }

  it should "create transformer of notification events to cc events" in {
    val jf = datagenFactory()
    val result = for (
      sourceEvents <- jf.randomBusinessEvents();
      interEvents <- jf.businessEventsToNotificationEvents(sourceEvents);
      resultEvents <- jf.notificationEventsToCcEvents(interEvents)) yield resultEvents
    result should not(be(None))
  }
}
