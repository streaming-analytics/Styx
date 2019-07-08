package ai.styx.domain.events

trait BaseEvent extends PayloadEvent with TimedEvent {
  def topic: String
} // raw event on any message bus

// just for testing
case class TestEvent(topic: String, eventTime: String, payload: Map[String, AnyRef]) extends BaseEvent

// base events
abstract class BaseRawEvent(topic: String, eventTime: String, payload: Map[String, AnyRef]) extends BaseEvent with TimedEvent

case class BasePatternEvent(topic: String, eventTime: String, event: String, payload: Map[String, AnyRef]) extends BaseEvent with TimedEvent

case class BaseNotificationEvent(topic: String, eventTime: String, payload: Map[String, AnyRef]) extends BaseEvent with TimedEvent

// derived detailed events for use cases
case class BaseTransactionEvent(topic: String, eventTime: String, accNum: Int, cardId: String, amount: Double, payload: Map[String, AnyRef]) extends BaseRawEvent(topic, eventTime, payload) with TimedEvent

case class BaseClickEvent(topic: String, eventTime: String, login: String, eventId: String, payload: Map[String, AnyRef]) extends BaseRawEvent(topic, eventTime, payload) with TimedEvent

case class BaseCcEvent(topic: String, eventTime: String, payload: Map[String, AnyRef]) extends BaseEvent with TimedEvent
