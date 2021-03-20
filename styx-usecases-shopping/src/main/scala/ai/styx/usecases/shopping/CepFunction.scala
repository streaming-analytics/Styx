package ai.styx.usecases.shopping

import ai.styx.domain.events.{BasePatternEvent, BaseTransactionEvent}
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.scala.DataStream

class CepFunction extends ai.styx.usecases.interfaces.CepFunction[BaseTransactionEvent] {
  implicit val typeInfo: TypeInformation[BasePatternEvent] = TypeInformation.of(classOf[(BasePatternEvent)])

  def map(sourceStream: DataStream[BaseTransactionEvent]): DataStream[BasePatternEvent] = {
    sourceStream.map(t => createBusinessEvent(t))
  }

  override def createBusinessEvent(transaction: BaseTransactionEvent): BasePatternEvent = {
    BasePatternEvent(transaction.topic, transaction.eventTime, "Test1", transaction.payload)
  }
}
