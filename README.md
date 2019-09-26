#Styx
_The multi-purpose STreaming AnalytYX platform._
Styx can be configured as you like, to work on a variety of streaming frameworks:
Flink or Spark Structured Streaming, Cassandra or Ignite, and Kafka. 
In this way, Styx supports the KFC (Kafka-Flink-Cassandra) and KISSS (Kafka-Ignite-SparkStructuredStreaming) stacks.

## Documentation
The architecture and technology (including code samples) are written in the form of reveal.js presentations, located in the presentations folder.
They can also be accessed on github.io, for example: https://streaming-analytics.github.io/Styx/presentations/styx.html#/ and https://streaming-analytics.github.io/Styx/presentations/strata2019.html#/.
Some online videos about Styx are on YouTube: https://www.youtube.com/watch?v=lfRDNW40rqY and https://www.youtube.com/watch?v=A2nQzSfZ3fE.

## Use cases
* Twitter trends
* Clickstream analysis
* Shopping pattern
* IoT
* ...

## Useful commands
Monitor dependent libraries for known, published vulnerabilities: 
```sbt dependencyCheckAggregate```

Check if any dependencies can be updated: ```sbt dependencyUpdates```

## Running instructions
### Flink
To start Flink in standalone mode on localhost:
```bash
bin/start-cluster.sh
```

### Spark
To start Spark in standalone mode on localhost:
```bash
sbin/start-all.sh
```

### Cassandra
To start Cassandra:
```bash
bin/cassandra -f
```

### Ingite
To start Ignite:
```bash
bin/ignite.sh
```

### Kafka
To start Kafka on MacOS with brew:
```bash
brew services start zookepeer
brew services start kafka
```

To start Kafka from a prompt:
```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties
```

To create a Kafka topic:
```bash
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic tweets
```

To start a Kafka producer:
```bash
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic tweets
```

To start a Kafka consumer:
```bash
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic tweets --from-beginning
```
