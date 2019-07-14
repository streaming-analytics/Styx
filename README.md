#Styx
_The multi-purpose STreaming AnalytYX platform based on Flink, Kafka and Cassandra_


## Running instructions
### Flink
To start Flink:
```bash

```

### Cassandra
To start Cassandra:
```bash
bin/cassandra -f
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

To stream a file to Kafka:
```bash
bin/
```

## Use cases
* Twitter trends
* Clickstream analysis
* Shopping pattern

## Useful commands
Monitor dependent libraries for known, published vulnerabilities: 
```sbt dependencyCheckAggregate```

Check if any dependencies can be updated: ```sbt dependencyUpdates```
