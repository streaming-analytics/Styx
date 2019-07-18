package ai.styx.frameworks.kafka

import java.util.Properties

import ai.styx.frameworks.interfaces.MessageBusConsumerFactory
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

class KafkaConsumerFactory extends MessageBusConsumerFactory {
  override def createMessageBusConsumer(properties: Properties): FlinkKafkaConsumer011[String] = {

    // SerializationFactory.createKeyedDeserializer[BaseEvent](rawEventFromPayload)
    //val schema = new BaseEventSchema()
    //val consumer = new FlinkKafkaConsumer011[BaseEvent]("topic1", schema, readProperties)

    val schema = new SimpleStringSchema()
    val consumer = new FlinkKafkaConsumer011[String](properties.getProperty("rawDataTopic"), schema, properties)

    consumer
  }
}
