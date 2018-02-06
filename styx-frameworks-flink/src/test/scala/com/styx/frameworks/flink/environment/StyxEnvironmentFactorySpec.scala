package com.styx.frameworks.flink.environment

import com.styx.common.BaseSpec
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.flink.api.common.JobExecutionResult
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

import scala.collection.JavaConverters._

class StyxEnvironmentFactorySpec extends BaseSpec {

  it should "setup flink environment as defined in config" in {
    val config: Config = prepareConfig
    val flinkEnvironment: StreamExecutionEnvironment = stubStreamExecutionEnvironment
    StyxEnvironmentFactory.setValuesFromConfig(flinkEnvironment, config)

    withClue("checkpointing interval") {
      flinkEnvironment.getCheckpointConfig.getCheckpointInterval shouldEqual  5342
    }
    withClue("checkpointing timeout") {
      flinkEnvironment.getCheckpointConfig.getCheckpointTimeout shouldEqual 5343
    }
    withClue("min pause between checkpoints") {
      flinkEnvironment.getCheckpointConfig.getMinPauseBetweenCheckpoints shouldEqual 5344
    }
    withClue("max concurrent checkpoints") {
      flinkEnvironment.getCheckpointConfig.getMaxConcurrentCheckpoints shouldEqual 5345
    }
    withClue("checkpointing mode") {
      flinkEnvironment.getCheckpointConfig.getCheckpointingMode shouldEqual CheckpointingMode.AT_LEAST_ONCE
    }
    withClue("buffer timeout") {
      flinkEnvironment.getBufferTimeout shouldEqual 154
    }
    withClue("env parallelism") {
      flinkEnvironment.getParallelism shouldEqual 65
    }
    withClue("max env parallelism") {
      flinkEnvironment.getMaxParallelism shouldEqual 67
    }
    withClue("number of execution retries") {
      flinkEnvironment.getNumberOfExecutionRetries shouldEqual 311
    }
    withClue("autowatermark interval") {
      flinkEnvironment.getConfig.getAutoWatermarkInterval shouldEqual 911
    }
  }

  def prepareConfig: Config = {
    ConfigFactory.parseMap(
      Map(
        "flink.checkpointing-interval" -> 5342,
        "flink.checkpointing-timeout" -> 5343,
        "flink.min-pause-between-checkpoints" -> 5344,
        "flink.max-concurrent-checkpoints" -> 5345,
        "flink.checkpointing-mode" -> "at-least-once",
        "flink.buffer-timeout" -> 154,
        "flink.env-parallelism" -> 65,
        "flink.max-env-parallelism" -> 67,
        "flink.number-of-execution-retries" -> 311,
        "flink.autowatermark-interval" -> 911
      ).asJava
    )
  }

  def stubStreamExecutionEnvironment: StreamExecutionEnvironment = {
    new StreamExecutionEnvironment(new org.apache.flink.streaming.api.environment.StreamExecutionEnvironment() {
      override def execute(jobName: String): JobExecutionResult = ???
    })
  }
}
