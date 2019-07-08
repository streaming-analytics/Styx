#!/usr/bin/env bash
# run this script from the folder where Spark is installed

# 1. start Spark standalone mode on localhost
sbin/start-all.sh

# 2. create a fat jar to deploy
sbt assembly

# 3.
bin/spark-submit --class ai.styx.app.pipeline.SparkJob --master spark://localhost:7077 target/scala-2.12/styx.jar
