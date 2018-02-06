package com.styx.frameworks.flink.environment

import com.typesafe.config.Config
import de.javakaffee.kryoserializers.jodatime.{JodaDateTimeSerializer, JodaLocalDateSerializer, JodaLocalDateTimeSerializer}
import com.styx.common.Logging
import com.styx.common.OptConfigImplicit._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.{CheckpointingMode, TimeCharacteristic}
import org.joda.time.{DateTime, LocalDate, LocalDateTime}

object StyxEnvironmentFactory extends Logging {

  def createEnvironment(styxConfig: Config): StreamExecutionEnvironment = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    setupEnvironment(env)
    env.getConfig.setGlobalJobParameters(new StyxJobParameters(styxConfig))
    setValuesFromConfig(env, styxConfig)
    logger.info("Flink environment - finished configuration")
    env
  }

  def setupEnvironment(env: StreamExecutionEnvironment): Unit = {
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)
    env.getConfig.disableSysoutLogging()
    env.registerTypeWithKryoSerializer(classOf[LocalDateTime], classOf[JodaLocalDateTimeSerializer])
    env.registerTypeWithKryoSerializer(classOf[DateTime], classOf[JodaDateTimeSerializer])
    env.registerTypeWithKryoSerializer(classOf[LocalDate], classOf[JodaLocalDateSerializer])
  }

  def setValuesFromConfig(env: StreamExecutionEnvironment, styxConfig: Config): Unit = {
    styxConfig.optLong("flink.checkpointing-interval").foreach(
      checkpointingInterval => {
        logger.info(s"Flink environment - setting checkpointing interval to $checkpointingInterval")
        env.enableCheckpointing(checkpointingInterval)
      }
    )
    styxConfig.optLong("flink.checkpointing-timeout").foreach(
      checkpointingTimeout => {
        logger.info(s"Flink environment - setting checkpointing timeout to $checkpointingTimeout")
        env.getCheckpointConfig.setCheckpointTimeout(checkpointingTimeout)
      }
    )
    styxConfig.optLong("flink.min-pause-between-checkpoints").foreach(
      minPauseBetweenCheckpoints => {
        logger.info(s"Flink environment - setting min pause between checkpoints to $minPauseBetweenCheckpoints")
        env.getCheckpointConfig.setMinPauseBetweenCheckpoints(minPauseBetweenCheckpoints)
      }
    )
    styxConfig.optInt("flink.max-concurrent-checkpoints").foreach(
      maxConcurrentCheckpoints => {
        logger.info(s"Flink environment - setting max concurrent checkpoints to $maxConcurrentCheckpoints")
        env.getCheckpointConfig.setMaxConcurrentCheckpoints(maxConcurrentCheckpoints)
      }
    )
    styxConfig.optString("flink.checkpointing-mode").flatMap {
      case "exactly-once" => Some(CheckpointingMode.EXACTLY_ONCE)
      case "at-least-once" => Some(CheckpointingMode.AT_LEAST_ONCE)
      case incorrectValue => logger.warn(s"Incorrect value for Flink environment checkpointing mode: $incorrectValue"); None
    }.foreach(
      checkpointingMode => {
        logger.info(s"Flink environment - setting checkpointing mode to $checkpointingMode")
        env.getCheckpointConfig.setCheckpointingMode(checkpointingMode)
      }
    )
    styxConfig.optLong("flink.buffer-timeout").foreach(
      bufferTimeout => {
        logger.info(s"Flink environment - setting buffer-timeout to $bufferTimeout")
        env.setBufferTimeout(bufferTimeout)
      }
    )
    styxConfig.optInt("flink.env-parallelism").foreach(
      envParallelism => {
        logger.info(s"Flink environment - setting parallelism to $envParallelism")
        env.setParallelism(envParallelism)
      }
    )
    styxConfig.optInt("flink.max-env-parallelism").foreach(
      maxEnvParallelism => {
        logger.info(s"Flink environment - setting max parallelism to $maxEnvParallelism")
        env.setMaxParallelism(maxEnvParallelism)
      }
    )
    styxConfig.optInt("flink.number-of-execution-retries").foreach(
      numberOfExecutionRetries => {
        logger.info(s"Flink environment - setting number of execution retries to $numberOfExecutionRetries")
        env.setNumberOfExecutionRetries(numberOfExecutionRetries)
      }
    )
    styxConfig.optLong("flink.autowatermark-interval").foreach(
      autowatermarkInterval => {
        logger.info(s"Flink environment - setting autowatermark interval to $autowatermarkInterval")
        env.getConfig.setAutoWatermarkInterval(autowatermarkInterval)
      }
    )
  }
}
