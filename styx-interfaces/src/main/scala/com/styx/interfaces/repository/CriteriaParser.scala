package com.styx.interfaces.repository

import com.styx.domain.Customer

trait CriteriaParser {
  def parseCustomerCriteria(criteria: String): Customer => Either[Seq[String],Boolean]
}
