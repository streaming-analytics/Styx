# Styx Shopping use case

This folder contains the Flink jobs for the Shopping use case.
The source events are transactions, e.g. money spend in shops from a debit card.
Once Styx detects the pattern that the total balance is running below zero, 
a notification can be send to the client.

The following core Flink jobs are available:
0. Data generator for creating a series of raw transaction events
0. CEP job for detecting the pattern 'a customer is running out of money'
0. ML job for scoring the combined features of the customer and the pattern event, possibly leading to an intermediate event, that is 1:1 with a notification event
0. Post Processor job for formatting the intermediate events, producing a final 'sink' on Kafka of notification events that can be picked up by a communication application

The jobs can be tested and deployed in various ways:
* StyxAppJob: one single Flink job, with storing all events on Kafka; this is recommended for getting the full functionality and logging all events
* StyxAppSingleJob: one single Flink job, without storing all events on Kafka; this is recommended for getting the full functionality and highest performance
* Single Kafka-in, Kafka-out jobs that perform a part of the functionality; this is recommended when jobs have to be deployed separately:
    * StyxDataGeneratorJob
    * StyxCepJob: CEP function, raw -> business events
    * StyxShoppingJob: ML function, business -> intermediate events
    * StyxPostProcessorJob: PP function, intermediate -> notification events
* Single jobs that perform a part of the functionality, and don't read or write to Kafka:
    * 
   
All these objects contain a main method that can be interpreted by Apache Flink.
To deploy, create a fat jar with ```sbt assembly``` and publish it to a running Flink cluster.
Then, specify the main method as the target.
Make sure to check the config files application.conf and reference.conf.
Examples of running jobs with local / embedded Flink, Cassandra, and Kafka are in the styx-appRunner module.
