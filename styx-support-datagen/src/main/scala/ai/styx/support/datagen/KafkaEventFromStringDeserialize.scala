package ai.styx.support.datagen

import ai.styx.domain.events.BaseKafkaEvent
import org.apache.flink.api.common.functions.RichMapFunction

class KafkaEventFromStringDeserialize(topicName: String) extends RichMapFunction[String, BaseKafkaEvent] {
  def map(in: String): BaseKafkaEvent = BaseKafkaEvent(topicName, RawEventGenerator.parseRawEvent(in))
}
