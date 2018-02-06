package com.styx.frameworks.flink.shopping

import com.styx.interfaces.repository.CustomerProfileRepository
import com.styx.domain.events.BaseTransactionEvent
import com.styx.common.Logging
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Try}

class RawEventToRepositorySink(repositoryCreate: => CustomerProfileRepository) extends RichSinkFunction[BaseTransactionEvent] with Logging {

  // RepositoryInstances are not serializable, thus
  @transient private lazy val repositoryInstance = repositoryCreate

  def invoke(re: BaseTransactionEvent): Unit = {
   Try {
      logger.trace(s"Writing raw event (transaction) for customer with ACC_NUM=${re.accNum} and CARD_ID=${re.cardId} to database.")
      repositoryInstance.addTransaction(re.accNum, re.cardId, re.trsTime, re.amount)
    } match {
     case Failure(t: Throwable) => logger.error(s"ERROR while writing raw event on to the transaction repository: ${t.getMessage}.")
     case _ => // ignore
   }
  }

}
