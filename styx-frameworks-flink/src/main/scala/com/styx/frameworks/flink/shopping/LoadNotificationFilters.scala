package com.styx.frameworks.flink.shopping

import java.util.Calendar

import com.styx.interfaces.repository.{CriteriaFilterRepository, CustomerProfileRepository, NotificationFilterRepository}
import com.styx.common.LogFutureImplicit._
import com.styx.common.LogOptionImplicit._
import com.styx.domain.events.BusinessEvent._
import com.styx.domain.events.{BaseBusinessEvent, CriteriaFilter}
import com.styx.domain.{Customer, NotificationFilter}
import com.styx.common.Logging
import org.apache.flink.streaming.api.scala.async.{AsyncFunction, ResultFuture}

import scala.concurrent.ExecutionContext.Implicits.global

class LoadNotificationFilters(profileRepositoryCreate: => CustomerProfileRepository,
                              notificationFilterRepositoryCreate: => NotificationFilterRepository) extends AsyncFunction[BaseBusinessEvent, (BaseBusinessEvent, Customer, Seq[NotificationFilter])] with Logging with Serializable {

  // RepositoryInstances are not serializable, thus
  @transient private lazy val profileRepository = profileRepositoryCreate
  @transient private lazy val notificationFilterRepository = notificationFilterRepositoryCreate

  override def asyncInvoke(businessEvent: BaseBusinessEvent, resultFuture: ResultFuture[(BaseBusinessEvent, Customer, Seq[NotificationFilter])]): Unit = {
    val time = Calendar.getInstance().getTime
    val timePreCql = Calendar.getInstance().getTimeInMillis

    logger.info("Received _business_ event for customer with ACC_NUM=" + businessEvent.accNum)
    // 1. get customer profile
    def customerProfileQuery = profileRepository.getCustomerProfile(businessEvent.accNum).logSuccess(_.logNone(logger.warn("Could not find customer profile.")))

    // 2. select relevant notification
    val notificationFiltersQuery = notificationFilterRepository.getNotificationFilters(businessEvent.payload("EVENT").toString)

    // if any fails (at any moment, immediately collect empty resultset such that flink can continue
    customerProfileQuery.onFailure { case e => resultFuture.complete(Iterable()) }
    notificationFiltersQuery.onFailure { case e => resultFuture.complete(Iterable()) }

    val dataFuture = for (customerProfileOpt <- customerProfileQuery;
                          notificationFilters <- notificationFiltersQuery
    ) yield (customerProfileOpt, notificationFilters)
    // Log if any of the futures fails
    dataFuture.logFailure(e => logger.error("Failed to determine BusinessEvent.", e))

    // When both futures succeed, forward the result
    dataFuture.onSuccess {
      case (None, _) =>
        resultFuture.complete(Iterable())
      case (Some(customerProfile), notificationFilters) =>
        resultFuture.complete(Iterable(
          (businessEvent
            .addTimeStamp("STARTFUNCTION", time.getTime)
            .addTimeStamp("PRECQL", timePreCql),
          customerProfile, notificationFilters)
        ))
    }
  }
}

// TODO: Refactor into single customer profile filter
class LoadCriteriaFilters(profileRepositoryCreate: => CustomerProfileRepository,
                          criteriaFilterRepositoryCreate: => CriteriaFilterRepository) extends AsyncFunction[BaseBusinessEvent, (BaseBusinessEvent, Customer, Seq[CriteriaFilter])] with Logging with Serializable {
  // RepositoryInstances are not serializable, thus
  @transient private lazy val profileRepository = profileRepositoryCreate
  @transient private lazy val criteriaFilterRepository = criteriaFilterRepositoryCreate

  override def asyncInvoke(businessEvent: BaseBusinessEvent, resultFuture: ResultFuture[(BaseBusinessEvent, Customer, Seq[CriteriaFilter])]): Unit = {
    val time = Calendar.getInstance().getTime
    val timePreCql = Calendar.getInstance().getTimeInMillis

    logger.info("Received _business_ event for customer with ACC_NUM=" + businessEvent.accNum)
    // 1. get customer profile
    def customerProfileQuery = profileRepository.getCustomerProfile(businessEvent.accNum).logSuccess(_.logNone(logger.warn("Could not find customer profile.")))

    // 2. select relevant selection criteria
    val businessEventFiltersQuery = criteriaFilterRepository.getCriteriaFilters(businessEvent.payload("EVENT_CD").toString)

    // if any fails (at any moment, immediately collect empty result set such that flink can continue
    businessEventFiltersQuery.onFailure { case _ => resultFuture.complete(Iterable()) }
    businessEventFiltersQuery.onFailure { case _ => resultFuture.complete(Iterable()) }

    val dataFuture = for (customerProfileOpt <- customerProfileQuery;
                          businessEventFilters <- businessEventFiltersQuery
    ) yield (customerProfileOpt, businessEventFilters)
    // Log if any of the futures fails
    dataFuture.logFailure(e => logger.error("Failed to determine BusinessEvent.", e))

    // When both futures succeed, forward the result
    dataFuture.onSuccess {
      case (None, _) =>
        resultFuture.complete(Iterable())
      case (Some(customerProfile), businessEventFilter) =>
        resultFuture.complete(Iterable(
          (businessEvent
            .addTimeStamp("STARTFUNCTION", time.getTime)
            .addTimeStamp("PRECQL", timePreCql),
            customerProfile, businessEventFilter)
        ))
    }
  }
}