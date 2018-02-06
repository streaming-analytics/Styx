package com.styx.frameworks.cassandra

import com.styx.common.LogFutureImplicit._
import com.styx.common.Logging
import com.styx.common.RetryHelper._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class CqlBatchExecutor extends Logging {

  case class BatchResult(total: Int, successes: Int, failures: Int)

  object BatchResult {
    def fromBooleans(bools: Seq[Boolean]) = BatchResult(bools.size, bools.count(identity), bools.count(! _))
  }

  def batchResultReduce(l: BatchResult, r: BatchResult) =
    BatchResult(l.total + r.total, l.successes + r.successes, l.failures + r.failures)

  /**
    * Applies a batch of insertions to cassandra without putting too much load on the cassandra database
    *
    * It is implemented by starting at most @batchSize threads, and then waiting till all of them are finished
    * before starting a new batch
    *
    * @param records   the objects to be sent to the database
    * @param statement the call to be done on a single record
    * @param batchSize the size of the batch.
    * @tparam T the object type, matching the argumen type of the statement
    * @return
    */
  def runBatch[T](records: Iterable[T], statement: T => Future[Boolean], batchSize: Int): Future[Boolean] =
  runBatch(records.toIterator, statement, batchSize)

  /**
    * Applies a batch of insertions to cassandra without putting too much load on the cassandra database
    *
    * It is implemented by starting at most @batchSize threads, and then waiting till all of them are finished
    * before starting a new batch
    *
    * @param records   the objects to be sent to the database
    * @param statement the call to be done on a single record
    * @param batchSize the size of the batch.
    * @tparam T the object type, matching the argumen type of the statement
    * @return
    */
  def runBatch[T](records: Iterator[T], statement: T => Future[Boolean], batchSize: Int): Future[Boolean] = {
    //Throttling is now done by chaining futures, such that one batch needs to be finished before the next can start
    //Throttling could also be done using a fixed thread pool, but that can cause deadlocks if called functions themselves also create new futures
    val emptyResultFuture = Future {
      BatchResult(0, 0, 0)
    }
    Future {
      records.grouped(batchSize)
        .foldLeft(emptyResultFuture) { case (prevResultFuture, recordSubset) =>
          //We will now flatmap (chain) the result of the previous batch, with a new future for the parallel execution of the current batch
          //This is wrapped with an Await to prevent that the foldleft will walk through the full list, creating too many futures
          //If we wouldn't do that, we would practically load the full file in memory, and concurrently create futures handling them
          //The await ensures we only use order of 1 batch of data in memory
          prevResultFuture.flatMap { prevResult => Await.ready(chainNextBatch(statement, recordSubset, prevResult), Duration.Inf)
          }
        }
        .logSuccess(s => logger.info(s"Finished without exceptions, out of ${s.total}  there were ${s.successes} successes and ${s.failures} failures."))
        .logFailure(f => logger.warn("Finished with exceptions", f))
        .map(_.failures == 0)
    }.flatMap(identity)
  }

  def chainNextBatch[T](statement: (T) => Future[Boolean], profiles: Seq[T], prevResult: BatchResult): Future[BatchResult] = {
    val futuresBatch = profiles.map(p => retryExecuteFuture(10)(statement(p)))
    val resultFuture = Future.sequence(futuresBatch)
    val deltaResultFuture = resultFuture.map(BatchResult.fromBooleans)
    deltaResultFuture.map { deltaResult =>
      val newResult = batchResultReduce(prevResult, deltaResult)
      logger.info(s"Running cql batch, currently have ${newResult.successes} successes and ${newResult.failures} failures out of the ${newResult.total} calls.")
      newResult
    }
  }
}
