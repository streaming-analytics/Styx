package ai.styx.frameworks.cassandra

import ai.styx.common.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, TimeoutException}

class CqlBatchExecutorSpec extends BaseSpec {

  case class TimedResult[T](duration: Duration, result: T)

  def time[R](name: String)(block: => R): TimedResult[R] = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    LOG.info(s"Elapsed time of $name: ${t1 - t0} ns")
    TimedResult((t1 - t0) nanoseconds, result)
  }

  def singleWait = 75 milliseconds

  // The Await's were timing out when coverage instrumentation was added, or when buildserver was 'slow'.
  // This corrects all delays by that slowness
  def testEnvironmentalDelay =
  (0 until 10)
    .par
    .map(_ =>
      time("testing how slow the environment is") {
        Await.result(Future {
          Thread.sleep(singleWait.toMillis)
        }, 2000 milliseconds)
      }.duration.minus(singleWait)
    ).toList
    .max.plus(5 milliseconds)

  "The batch executor" should "succeed faster than sum of independent futures" in {
    val batchSize = 4
    val numRecords = 16
    val environmentalDelay = testEnvironmentalDelay
    val records = (0 until 16).toIterator
    val handler: Int => Future[Boolean] = s => Future {
      Thread.sleep(singleWait.toMillis); true
    }
    val executor = new CqlBatchExecutor()
    val sequentialExecutionDuration = singleWait.plus(environmentalDelay).mul(numRecords)
    val batchRunner = time("runBatch")(executor.runBatch(records, handler, batchSize))
    val batchResult = time("await result")(Await.result(batchRunner.result, sequentialExecutionDuration))
    val totalDuration = batchRunner.duration.plus(batchResult.duration)
    LOG.info(s"Total duration = $totalDuration")
    LOG.info(s"Sequential execution duration = $sequentialExecutionDuration")
    val numBatches = math.ceil(numRecords / batchSize.toDouble)

    withClue("Number of batch durations it took for initiating the sequence of batches") {
      batchRunner.duration.minus(singleWait).div(singleWait) shouldBe <(1.0)
    }
    withClue("Completion faster than sequential execution of individual tasks? (to verify we don't do sequential execution)") {
      totalDuration.minus(environmentalDelay) shouldBe <[Duration](singleWait.plus(environmentalDelay).mul(numRecords))
    }
    withClue("Completion slower than 1 individual task? (to verify we don't do full paralellism)") {
      totalDuration shouldBe >[Duration](singleWait)
    }
    withClue("Number of sequential waits compared to used batches (lower bound) ") {
      totalDuration.minus(environmentalDelay.mul(numRecords)).div(singleWait) shouldBe <(numBatches + 1)
    }
    withClue("Number of sequential waits compared to used batches") {
      totalDuration.div(singleWait) shouldBe >(numBatches - 1)
    }
  }

  it should "propagate returned false" in {
    val records: Iterable[Int] = 0 until 10
    val environmentalDelay = testEnvironmentalDelay
    val handler: Int => Future[Boolean] = s => Future {
      Thread.sleep(singleWait.toMillis); s != 5
    } // fails 1 times
    val result = new CqlBatchExecutor().runBatch(records, handler, 5)
    Await.result(result, singleWait.mul(records.size).plus(environmentalDelay)) should be(false)
  }

  it should "NOT implement fail fast based on returned false" in {
    val records: Iterable[Int] = 0 until 10
    val environmentalDelay = testEnvironmentalDelay
    val batchSize = 3
    val handler: Int => Future[Boolean] = s => Future {
      Thread.sleep(singleWait.toMillis); s != 0
    } // fails the first batch
    val executor = new CqlBatchExecutor()
    val batchRunner = time("runBatch")(executor.runBatch(records, handler, batchSize))
    val batchResult = time("await result")(Await.result(batchRunner.result, singleWait.mul(records.size).plus(environmentalDelay)))
    val totalDuration = batchRunner.duration.plus(batchResult.duration)
    val numBatches = math.ceil(records.size / batchSize.toDouble)

    withClue("Number of sequential waits compared to used batches") {
      totalDuration.div(singleWait) shouldBe >(numBatches - 0.9)
    }
  }

  it should "implement lazy list traversal" in {
    val environmentalDelay = testEnvironmentalDelay
    val almostInfiniteItems: Iterable[Int] = 0 until 100
    val handler: Int => Future[Boolean] = s => Future {
      Thread.sleep(singleWait.toMillis); s != 0
    } // fails the first batch
    val executor = new CqlBatchExecutor()
    val batchRunner = time("initiate Future of runBatch")(Future {
      executor.runBatch(almostInfiniteItems, handler, batchSize = 4)
    }.flatMap(identity))
    withClue("Number of individual waits it took for initiating the sequence of batches") {
      batchRunner.duration.minus(environmentalDelay).div(singleWait) shouldBe <(1.0)
    }
    withClue("Is the future finished after a few waits?") {
      assertThrows[TimeoutException] {
        Await.ready(batchRunner.result, singleWait.mul(5)).isCompleted should be(false)
      }
    }
  }
}
