package ai.styx.domain.events

import java.sql.Timestamp

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.{JsonFormat, JsonIgnoreProperties, JsonProperty}
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer

import scala.beans.BeanProperty

@JsonIgnoreProperties(ignoreUnknown = true)
case class Transaction (
                         @BeanProperty
                         @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EE MMM dd HH:mm:ss Z yyyy", locale = "en")
                         @JsonProperty("created_at")
                         @JsonDeserialize(using = classOf[TimestampDeserializer])
                         created_at: Timestamp = null,

                         @BeanProperty
                         @JsonProperty("customer_id")
                         customer_id: String = null,

                         @BeanProperty
                         @JsonProperty("amount")
                         amount: Double = 0,

                         @BeanProperty
                         @JsonProperty("type")
                         `type`: String = "",

                         @BeanProperty
                         @JsonProperty("currency")
                         currency: String = "EURO",

                         @BeanProperty
                         @JsonProperty("counter_account")
                         counter_account: String = "",

                         @BeanProperty
                         @JsonProperty("text")
                         text: String = null
                         // no other fields needed for now
                       ) {
  def toJson(): String = Tweet.objectMapper.writeValueAsString(this)
}
