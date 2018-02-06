package com.styx.frameworks.cassandra

import com.datastax.driver.core.{ResultSet, Statement}
import com.styx.common.Logging
import com.styx.common.LogFutureImplicit._
import com.styx.common.LogTryImplicit._
import FutureImplicit._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object CqlAttemptorImplicit {
  implicit class CqlAttemptor(val repo: CassandraRepository) extends Logging{

    def tryStatement(statement: Statement, metricsName: String, description: String): Try[ResultSet] = {
      logger.info(s"Starting cassandra call '$description'")
      Try {
        repo.session.execute(statement) // executeWithMetrics(statement, statement.getConsistencyLevel, true, metricsName)
      }
        .logFailure(e => logger.error(s"Failure in cassandra call '$description'", e))
        .logSuccess(_ => logger.debug(s"Successfully performed cassandra call '$description'"))
    }

    def tryAsyncStatement(statement: Statement, metricsName: String, description: String)(implicit ec: ExecutionContext): Future[ResultSet] = {
      logger.info(s"Starting cassandra call '$description'")
      Future {
        repo.session.executeAsync(statement)  //executeAsyncWithMetrics(statement, statement.getConsistencyLevel, true, metricsName)
          .asScala //Future[ResultSet] but throws exceptions
      }
        // now we have Future[Future[ResultSet]]
        .logFailure(e => logger.error(s"Failure in initiation of cassandra  call '$description'", e))
        .logSuccess(_ => logger.debug(s"Successfully initiated cassandra call '$description'"))
        .flatMap(identity) // identical to .flatten => we get a Future[ResultSet]
        .logSuccess(posts => logger.info(s"Successfully performed async cassandra call '$description'"))
        .logFailure(e => logger.error(s"Failure in async cassandra call '$description'", e))
    }
  }
}
