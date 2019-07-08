package ai.styx.frameworks.kafka

import java.util

class PayloadSerializer extends org.apache.kafka.common.serialization.Serializer[Map[String, AnyRef]] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ()

  override def serialize(topic: String, data: Map[String, AnyRef]): Array[Byte] =
    scala.util.parsing.json.JSONObject(data).toString().getBytes()

  override def close(): Unit = ()
}