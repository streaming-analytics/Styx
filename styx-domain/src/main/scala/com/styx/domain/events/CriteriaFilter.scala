package com.styx.domain.events

import com.styx.domain.Customer

case class CriteriaFilter(Name: String, Event: String, Criteria: Customer=>Either[Seq[String],Boolean])
