package ai.styx.frameworks.kafka

import java.util.Properties

import ai.styx.common.Configuration
import ai.styx.frameworks.interfaces.{MessageBusConsumer, MessageBusConsumerFactory}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, FlinkKafkaConsumer011}

object KafkaConsumerFactory extends MessageBusConsumerFactory {
  override def createMessageBusConsumer(config: Configuration): MessageBusConsumer = {

    // SerializationFactory.createKeyedDeserializer[BaseEvent](rawEventFromPayload)
    //val schema = new BaseEventSchema()
    //val consumer = new FlinkKafkaConsumer011[BaseEvent]("topic1", schema, readProperties)

    //val schema = new SimpleStringSchema()
    //val consumer = new FlinkKafkaConsumer011[String](properties.getProperty("rawDataTopic"), schema, properties)


    new KafkaStringConsumer(config)
  }
}
