#!/usr/bin/env bash

# start one node
/opt/flink/bin/flink run -p 4 -c ai.styx.StyxAppJob /tmp/styx-app/target/styx-app-assembly-0.0.1-SNAPSHOT.jar --config /opt/flink/jars/reference.conf
/opt/flink/bin/flink run -p 4 -c ai.styx.StyxAppJob /tmp/styx-app/target/styx-app-0.0.1-dev.71-assembly.jar --config /opt/flink/jars/reference.conf

# submit poisonpill
java -cp /tmp/styx-appRunner/target/styxAppRunner-assembly-0.0.1-SNAPSHOT.jar ai.styx.StyxRawDataProducer --config /opt/flink/jars/reference.conf --styx.datagen.datafile /opt/hadoop-2.7.3/data/poisonpill.csv

# show log file:
ls -altr /opt/flink/log/

# search for errors in log files:
grep -nr 'ERROR' /var/log/flink/log

# show the current list of jobs"
/opt/flink/bin/flink list

# copy alphanumeric job id"
/opt/flink/bin/flink savepoint [JOB_ID]

# wait for response: gives savepoint id"
/opt/flink/bin/flink cancel [JOB_ID]

# start the new job"
/opt/flink/bin/flink run -p 4 -s jobmanager://savepoints/[SAVEPOINT_ID] -c ai.styx.StyxAppJob /tmp/styx-app/target/styx-app-0.0.1-dev.71-assembly.jar --config /opt/flink/jars/reference.conf
/opt/flink/bin/flink run -p 4 -s jobmanager://savepoints/[SAVEPOINT_ID] -c ai.styx.StyxAppJob /tmp/styx-app/target/styx-app-assembly-0.0.1-SNAPSHOT.jar --config /opt/flink/jars/reference.conf
