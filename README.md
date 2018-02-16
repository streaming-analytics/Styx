![Styx logo](styx.gif "Styx logo")

[![Build Status](https://travis-ci.org/streaming-analytics/Styx.svg?branch=master)](https://travis-ci.org/streaming-analytics/Styx)

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7253c7bcef0e41c49140bafc3720d2fa)](https://www.codacy.com/app/bas/Styx?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=streaming-analytics/Styx&amp;utm_campaign=Badge_Grade)

**Styx** is the STreaming AnalytYX engine that can be used for several use cases, for example:
* producing actions or insights in real-time data
* predictive maintenance on systems that produce event data
* providing recommendations based on click events

Styx is build with the following technology:
* Apache Flink
* Apache Kafka
* Apache Cassandra
* Openscoring.io

The entire application is written in Scala.
For scoring the machine learning models, we load them in PMML format.
The Protobuf serialization format is used to store messages on the Kafka bus.  

A use case can consist of multiple stages in a streaming data pipeline. A typical use case follows a pattern of three steps:
1. Complex Event Processing Engine: combining real-time 'raw' events with historical data to produce an interesting 'business event';
2. Machine Learning Engine: business events are evaluated by a real-time scoring (evaluation) engine, where machine learning models or business rules are being applied on the events combined with 'static' data from a database or external APIs. The result is an 'intermediate event';
3. Post Processor: the handling of the intermediate events are transformed into a useful action or notification event, for example an XML message that produces and email or a push notification on a mobile app.

The events are all stored on a Kafka message bus, so Styx is essentially a Kafka-in, Kafka-out framework 
that contains all the business logic.
In a brief overview, this leads to the following architecture:

_Raw Event_ => **CEP Engine** => _Business Event_ => **ML Engine** => _Intermediate Event_ => **Post Processor** => _Action/Notification Event_

Abbreviations
-------------
* CEP = Complex Event Processor
* ML = Machine Learning
* PP = Post-Processor

Example use case
----------------
The code contains an example use case called *shopping*.
In this use case, payment transactions are processed as _raw events_.
The **CEP Engine** uses pattern recognition logic to detect if a customer is shopping and potentially running out of money on his or her running bank account.
If that is the base (_business event_), in the **ML Engine** a model is scored to determine the best action for each individual customer. This action is stored as an _intermediate event_.
Finally, the **Post Processor** transforms the notification into a real action for the customer, e.g. 'Transfer money from my savings account'.

Performance
-----------
Styx has been tested on a 10-node Flink cluster, where 10 million random events were loaded on the Kafka cluster.
The example use case (shopping detection and notification) performed as follows:
* maximum throughput of entire data pipeline: ~450k events per second
* average throughput of entire data pipeline: ~100k events per second
* average latency of 95-percentile of events: 42ms

Development environment
-------------
To run the Flink job locally:
```bash
sbt "project styxAppRunner" run
```

To run all unit and integration tests, and produce a code coverage report:
```base
sbt clean coverage test
```

To package the code into a binary 'fat jar':
```bash
sbt assembly
```

Data Generator
-------------
To run the data generator:
```bash
java -jar customer-profile-0.4-java-1.7.jar --totalNumber=6000000
java -Xms1G -Xmx10G -jar generator-2017-01-02-V2-timestamp.jar
```

Project Structure
-------------
* styx-app: the main bootstrapped Flink jobs
* styx-appRunner: a helper standalone app which includes the Flink dependencies, that can be used to run and test locally
* styx-commons: common independent classes for logging, exception handling, file I/O, configuration, etc.
* styx-domain: the core domain entities of the use cases
* styx-interfaces: the technology-independent abstract classes and traits 
* styx-frameworks-flink: the business logic of the streaming data processing engine
* styx-frameworks-cassandra: the database connections
* styx-frameworks-kafka: the message bus connections
* styx-frameworks-openscoring: the model serving and scoring mechanism
* styx-flink-core - the use case independent classes (most inner dependency, shouldn't depend on kafka/cassandra)
* styx-flink-kafkaconnect - provides streams that read / write  from/to kafka
* styx-support-db: all database-related CQL scripts
* styx-support-datagen: a data generator that puts messages on the Kafka bus
* styx-support-deployment-validator: a tool that validates an use case end-to-end
* styx-support-ops: scripts to deploy and test the application
