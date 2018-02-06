#!/usr/bin/env bash

# To submit the Flink job:
/opt/flink/bin/flink run -p 16 -c com.styx.StyxAppJob /tmp/styx-app/target/styx-app-assembly-0.0.1-SNAPSHOT.jar --config /opt/flink/jars/reference.conf

# To run the data generator (putting raw events from a local file onto kafka):
java -cp /tmp/styx-appRunner/target/styxAppRunner-assembly-0.0.1-SNAPSHOT.jar com.styx.StyxRawDataProducer --config /opt/flink/jars/reference.conf

# To run just the CEP engine, using random raw events produced from within the flink process:
/opt/flink/bin/flink run -p 16 -c com.styx.StyxAppKafkaLessCepJob /tmp/styx-app/target/styx-app-assembly-0.0.1-SNAPSHOT.jar --config /opt/flink/jars/reference.conf

# To run just the Styx job, using random BUSINESS events produced from within the flink process:
/opt/flink/bin/flink run -p 16 -c com.styx.StyxAppKafkaLessJob /tmp/styx-app/target/styx-app-assembly-0.0.1-SNAPSHOT.jar --config /opt/flink/jars/reference.conf
