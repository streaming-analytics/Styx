package ai.styx.frameworks.kafka

import ai.styx.common.Configuration
import ai.styx.frameworks.interfaces.MessageBusConsumer
import org.apache.flink.api.common.serialization.SimpleStringSchema
//import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

class KafkaStringConsumer(config: Configuration)
  extends FlinkKafkaConsumer011[String](config.kafkaConfig.rawDataTopic, new SimpleStringSchema(), config.kafkaConsumerProperties)
    with MessageBusConsumer {
}
