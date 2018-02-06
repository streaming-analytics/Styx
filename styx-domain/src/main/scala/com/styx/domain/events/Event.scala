package com.styx.domain.events

import org.joda.time.DateTime

abstract class Event

case class BaseKafkaEvent(topic: String, payload: Map[String, AnyRef]) extends PayloadEvent // raw event on any Kafka bus

case class BaseNotificationEvent(eventTime: DateTime, payload: Map[String, AnyRef]) extends Event with TimedEvent with PayloadEvent

case class BaseTransactionEvent(eventTime: DateTime, accNum: Int, cardId: String, amount: Double, trsTime: DateTime, payload: Map[String, AnyRef]) extends Event with TimedEvent with PayloadEvent

case class BaseBusinessEvent(eventTime: DateTime, accNum: Int, cardId: String, event: String, payload: Map[String, AnyRef]) extends Event with TimedEvent with PayloadEvent

case class BaseCcEvent(eventTime: DateTime, payload: Map[String, AnyRef]) extends Event with TimedEvent with PayloadEvent

case class BaseClickEvent(eventTime: DateTime, login: String, eventId: String, payload: Map[String, AnyRef]) extends Event with TimedEvent with PayloadEvent // formatted event on TPA Kafka bus placed by adapter
