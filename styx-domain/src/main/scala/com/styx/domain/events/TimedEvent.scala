package com.styx.domain.events

import org.joda.time.DateTime

trait TimedEvent {def eventTime: DateTime}
