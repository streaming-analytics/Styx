package ai.styx.frameworks.kafka

import ai.styx.common.Configuration
import ai.styx.frameworks.interfaces.MessageBusConsumer
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer

class KafkaStringConsumer(config: Configuration)
  extends FlinkKafkaConsumer[String](config.kafkaConfig.rawDataTopic, new SimpleStringSchema(), config.kafkaConsumerProperties)
    with MessageBusConsumer {
}
