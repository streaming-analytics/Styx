package com.styx.frameworks.flink.shopping

import com.styx.interfaces.repository.CustomerProfileRepository
import com.styx.common.LogTryImplicit._
import com.styx.domain.events.{BaseBusinessEvent, BaseTransactionEvent, BusinessEvent}
import com.styx.common.Logging
import org.apache.flink.api.common.functions.RichMapFunction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class CepFunction(repositoryCreate: => CustomerProfileRepository, featureToggles: FeatureToggles) extends RichMapFunction[(ResultingBalance, BaseTransactionEvent), Option[BaseBusinessEvent]] with Logging {

  val balanceThreshold: Int = repositoryCreate.getCepBalanceThreshold.getOrElse{
    logger.warn("No valid balance threshold found for CEP Shopping use case")
    0
  }

  def map(balanceTrigger: (ResultingBalance, BaseTransactionEvent)): Option[BaseBusinessEvent] = balanceTrigger match {
    case (balance, transactionEvent) =>
      if (featureToggles.poisonPill && transactionEvent.cardId == "-1" && transactionEvent.accNum == -1)
        Try {
          throw PoisonPillException("This is an inserted bug to show what happens during a failure.")
        }.logFailure(e => logger.error("Exception during processing.", e))

      logger.trace(s"Received _raw_ event for customer with ACC_NUM=${transactionEvent.accNum}")
      createBusinessEvents(transactionEvent, balance.value)
  }

  def createBusinessEvents(transactionEvent: BaseTransactionEvent, resultingBalance: Double): Option[BaseBusinessEvent] = {
    // 1. retrieve possible CEP Engine configurations for customer, using event type and applying selection criteria
    // 2. for each CEP Config: pattern match, apply pmml model (detect shopping pattern)
    // 3. for each CEP Config: output to kafka, to be picked up by Styx. the target EVENT value is in the business_event column
    // for now, just create a Shopping business event for every raw event that comes in.
    if (resultingBalance < balanceThreshold) {
      val event = "Shopping"
      Some(BusinessEvent.from(transactionEvent, event, resultingBalance))
    } else {
      None
    }
  }
}
