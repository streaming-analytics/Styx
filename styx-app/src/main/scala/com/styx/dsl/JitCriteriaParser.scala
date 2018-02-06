package com.styx.dsl

import com.styx.common.Logging
import com.styx.interfaces.repository.CriteriaParser
import fastparse.core.Parsed
import com.styx.domain.Customer
import com.styx.dsl.UsefulImplicits._

class JitCriteriaParser extends CriteriaParser with Logging {

  def parseCustomerCriteria(criteria: String): Customer => Either[Seq[String], Boolean] = customer => {
    logger.debug("ANC - parsing " + criteria)
    Parsing.ComplexExprs.E.parse(criteria) match {
      case Parsed.Success(expression, _) =>
        val customerMap:Map[String,Any] = customer
        val dataSource:DataSource = customerMap
        val result = expression.evaluate(dataSource)
        result match {
          case Right(True) => Right(True)
          case Right(False(reasons)) => Left(reasons)
          case Left(text) => Left(Seq(text))
        }
      case Parsed.Failure(_, index, _) => Left(Seq(s"Failed to parse criteria at index $index."))
    }
  }
}
