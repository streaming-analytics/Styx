package com.styx.frameworks.flink.shopping

import java.util.Calendar

import com.styx.common.LogTryImplicit._
import com.styx.domain.events.BusinessEvent._
import com.styx.domain.events.NotificationEvent._
import com.styx.domain.events.{BaseBusinessEvent, BaseNotificationEvent, NotificationEvent}
import com.styx.domain.{Customer, NotificationFilter}
import com.styx.common.Logging
import org.apache.flink.api.common.functions.RichMapFunction

import scala.util.Try

class MlFunction() extends RichMapFunction[(BaseBusinessEvent, Customer, Seq[NotificationFilter]), Option[BaseNotificationEvent]] with Logging {

  def map(event: (BaseBusinessEvent, Customer, Seq[NotificationFilter])): Option[BaseNotificationEvent] = event match {
    case (businessEvent, customerProfile, notificationFilters) =>

    logger.info("Found customer")
    val timePostCql = Calendar.getInstance().getTimeInMillis
    // 3. apply selection criteria for each notification for customer
    logger.info(s"Verifying the ${notificationFilters.size} notification filters.")
      Try {
        val scores = for(model <- notificationFilters) yield {
          // 4. for each notification: predict relevancy by scoring pmml model
          logger.info(s"Scoring the model for ${model.Name}")
          (model, model.Model.score(customerProfile))
        }

        // 5. for each notification: compare the relevancy score to threshold
        scores.filter{ case (model, score) =>
          logger.info(s"Comparing score $score with threshold ${model.Threshold}")
          score >= model.Threshold
        }
          .map { case (filter, score) =>
            createNotificationEvent(businessEvent.addTimeStamp("POSTCQL", timePostCql), customerProfile, filter, score)
          }
          .headOption
          .map(_.addTimeStamp("FINAL"))
        // possible improvement here: don't just take the first element, but output all notifications. Change function output type to List[NotificationEvent]?
      }.logFailure(e=>logger.error("Failed to determine scoring", e))
        .getOrElse(None)
  }

  def createNotificationEvent(businessEvent: BaseBusinessEvent, customerProfile: Customer, filter: NotificationFilter, score: Double): BaseNotificationEvent = {
    // 6. for each notification: output (to be picked up by post-processor) if the score is bigger than the threshold
    val balance = prettifyBalance(businessEvent.payload)
    logger.info(s"Corresponding 'nice' balance is: $balance")
    NotificationEvent.from(
      businessEvent.addTimeStamp("SCORED")
      , filter, score, balance, s"${businessEvent.event} - ${messageType(customerProfile)}")
      .addTimeStamp("TONOTIF")
  }

  def extractDouble(x: Any): Double = x match {
    case x: Double => x
    case _ => throw new ClassCastException("Could not cast to Double")
  }

  def prettifyBalance(payload: Map[String, AnyRef]): Double = {
    BigDecimal(extractDouble(payload("BALANCE")))
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)
      .toDouble
  }

  def messageType(customer: Customer): String = {

    if (customer.Segment contains "HI") "Warning"
    else if ("HI" equals customer.Rating) "Offer Account Debit"
    else if ("Y" equals customer.Budget) {
      if ("Y" equals customer.CreditCard) "Encourage"
      else "Transfer"
    }
    else {
      if ("Y" equals customer.CreditCard)
        "Warning"
      else {
        if ("LO" equals customer.Rating) "Warning"
        else "OfferInquiry"
      }
    }
  }
}

