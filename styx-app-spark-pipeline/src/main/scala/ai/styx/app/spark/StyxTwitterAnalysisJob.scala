package ai.styx.app.spark

import ai.styx.common.Logging
import org.apache.spark.sql.SparkSession

object StyxTwitterAnalysisJob extends App with Logging {
  LOG.info("Spark version " + org.apache.spark.SPARK_VERSION)

  // connect to Spark
  val spark = SparkSession
    .builder
    .appName("Styx")
    .config("spark.master", "local")
    .getOrCreate()

  // get the data from Kafka: subscribe to topic
  val df = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "tweets")
    .load()

  val ds = df.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    .writeStream
    .format("console")
    .start()

  df.printSchema()
  ds.awaitTermination()

  ///// part 1a CEP: count the words per period /////

  ///// part 1b CEP: look at 2 periods (e.g. days) and calculate slope, find top 5 /////

  ///// #2: ML, get notification /////

  ///// #3: Notification /////


  // sink the data to Kafka
  //  val ds = df
  //    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
  //    .writeStream
  //    .format("kafka")
  //    .option("kafka.bootstrap.servers", "host1:port1,host2:port2")
  //    .option("topic", "topic1")
  //    .start()

}
