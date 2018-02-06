package com.styx.domain.events

trait PayloadEvent {
  def payload: Map[String, AnyRef]
}
