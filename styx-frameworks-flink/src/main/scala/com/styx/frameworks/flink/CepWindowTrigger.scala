package com.styx.frameworks.flink

import com.styx.domain.events.TimedEvent
import com.styx.common.Logging
import org.apache.flink.api.common.state.{ListState, ListStateDescriptor, ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.windowing.triggers.Trigger.TriggerContext
import org.apache.flink.streaming.api.windowing.triggers.{Trigger, TriggerResult}
import org.apache.flink.streaming.api.windowing.windows.Window

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

class CepWindowTrigger[E <: TimedEvent, W <: Window](period: Duration, minCountTrigger: Int) extends Trigger[E, W] with Logging {
  private val periodMillis = period.toMillis
  private val lastTimestampDescriptor: ValueStateDescriptor[java.lang.Long] = new ValueStateDescriptor[java.lang.Long]("SessionTrigger.timestamp", classOf[java.lang.Long])
  private val windowedEvents: ListStateDescriptor[(Long, E)] = new ListStateDescriptor[(Long, E)]("SessionTrigger.item", classOf[(Long, E)])

  case class State(ctx: TriggerContext, lastTimestampState: ValueState[java.lang.Long], events: ListState[(Long, E)]) {

    def eventCount = {
      getEvents.size
    }

    def lastTimeStamp: Option[Long] = {
      Option(lastTimestampState.value()).map(_.toLong)
    }

    def writeLastTimeStamp(value: Long): Unit = {
      lastTimestampState.update(java.lang.Long.valueOf(value))
    }

    def getEvents: Iterable[(Long, E)] = {
      Option(events.get()).map(_.asScala).getOrElse(Seq())
    }

    /**
      * Filters all collected items, requires their timestamp to be STRICTLY at least minTimestamp
      *
      * @param minTimestamp STRICT lower bound on timestamp
      * @return the collection of events
      */
    def filterWindow(minTimestamp: Long): Unit = {
      lastTimeStamp match {
        case None =>
          clearWindow()
        case Some(lastTimeStampValue) =>
          if (lastTimeStampValue < minTimestamp) {
            // We know nothing would pass the filter
            clearWindow()
          } else {
            val filteredEvents = getEvents.filter { case (ts, event) => minTimestamp < ts }
            //Perhaps could speed things up by using a mutable state.... deleting on iterator only works for memorybackend
            events.clear()
            filteredEvents.foreach(events.add)
          }
      }
    }

    def addToWindow(timestamp: Long, element: E): Unit = {
      removeTimeoutTrigger()
      events.add((timestamp, element))
      writeLastTimeStamp(timestamp)
      addTimeoutTriggerFor(timestamp)
    }

    def addTimeoutTriggerFor(timestamp: Long): Unit = {
      ctx.registerEventTimeTimer(timestamp + periodMillis) // Register event on moment last element expires
    }

    def removeTimeoutTrigger(): Unit = {
      lastTimeStamp.foreach(lastTs => ctx.deleteEventTimeTimer(lastTs + periodMillis) )
    }

    def isValidTimeOut(time: Long): Boolean = {
      lastTimeStamp match{
        case None => true
        case Some(lastTimestampValue) => lastTimestampValue + periodMillis < time
      }
    }

    def clearWindow(): Unit = {
      removeTimeoutTrigger()
      events.clear()
      lastTimestampState.clear()
    }

  }

  object State {
    def apply(ctx: TriggerContext): State = new State(ctx, ctx.getPartitionedState(lastTimestampDescriptor), ctx.getPartitionedState(windowedEvents))
  }

  def onElement(element: E, timestamp: Long, window: W, ctx: TriggerContext): TriggerResult = {
    val state = State(ctx)
    state.filterWindow(timestamp - periodMillis)
    state.addToWindow(timestamp, element)
    val itemCount = state.eventCount
    if (itemCount >= minCountTrigger) {
      logger.debug(s"Received element nr $itemCount , thus FIREing window. Element = $element")
      TriggerResult.FIRE
    } else {
      logger.trace(s"Timer triggered after $period , but new items found thus CONTINUEing with this window.")
      TriggerResult.CONTINUE
    }
  }

  def onProcessingTime(time: Long, window: W, ctx: TriggerContext): TriggerResult =
    throw new UnsupportedOperationException("This trigger won't work for processing time")

  def onEventTime(time: Long, window: W, ctx: TriggerContext): TriggerResult = {
    val state = State(ctx)
    if (state.isValidTimeOut(time)) {
      logger.trace(s"Timer triggered after $period , no new items found thus PURGEing the window.")
      TriggerResult.PURGE
    } else{
      TriggerResult.CONTINUE
    }
  }

  override def clear(window: W, ctx: TriggerContext): Unit = {
    val state = State(ctx)
    logger.info(s"External request to CLEAR the window. Removing the ${state.eventCount} collected events.")
    state.clearWindow()
  }

}
