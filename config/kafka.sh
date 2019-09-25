#!/usr/bin/env bash
# run this script from the folder where Kafka is installed

# 1. start Zookeeper and Kafka
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties

# 2. create topic:
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic clickstream

# 3. load data to Kafka via a pipe:
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic clickstream < click_browsing_data.csv

# 4. (to test) consume data from Kafka:
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic clickstream --from-beginning

# list topics:
bin/kafka-topics.sh --create --zookeeper localhost:2181 --list

# delete topic:
bin/kafka-topics.sh --delete --zookeeper localhost:2181 --topic clickstream
