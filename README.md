#Styx
##The multi-purpose STreaming AnalytYX platform based on Flink, Kafka and Cassandra

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
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
```

To start a Kafka producer:
```bash
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
```

To start a Kafka consumer:
```bash
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
```
