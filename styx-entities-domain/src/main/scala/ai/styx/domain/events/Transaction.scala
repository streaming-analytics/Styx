package ai.styx.domain.events

import java.sql.Timestamp

import ai.styx.common.Logging
import ai.styx.domain.DomainEntity
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.{JsonFormat, JsonIgnoreProperties, JsonProperty}
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.deser.std.DateDeserializers.TimestampDeserializer
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import org.joda.time.DateTime

import scala.beans.BeanProperty

@JsonIgnoreProperties(ignoreUnknown = true)
case class Transaction (
                   @BeanProperty
                   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss Z", locale = "en")
                   @JsonProperty("time")
                   @JsonDeserialize(using = classOf[TimestampDeserializer])
                   time: Timestamp = null,

                   @BeanProperty
                   @JsonProperty("description")
                   description: String = null,

                   @BeanProperty
                   @JsonProperty("amount")
                   amount: Double = 0.0,

                   @BeanProperty
                   @JsonProperty("currency")
                   currency: String = "EURO"
                       ) {
  def created: Option[DateTime] = {
    try {
      Some(new DateTime(time.getTime))
    }
    catch {
      case t: Throwable => None
    }
  }

  def toJson(): String = Transaction.objectMapper.writeValueAsString(this)
}

object Transaction extends Logging {
  private val mapper: ObjectMapper = new ObjectMapper()
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
  mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)

  def objectMapper: ObjectMapper = mapper

  // TODO: Option
  def fromString(s: String): Transaction = {
    try {
      val transaction = mapper.readValue(s, classOf[Transaction]) // .replaceAll("[$\\[\\]{}]", "")
      if (transaction == null || transaction.created.isEmpty) null else transaction
    }
    catch {
      case t: Throwable =>
        LOG.error(s"Unable to parse transaction from string $s", t)
        null
    }
  }

  def fromJson(json: String): Option[Transaction] = {
    try {
      val transaction = mapper.readValue(json, classOf[Transaction]) // .replaceAll("[$\\[\\]{}]", "")
      val maybeTransaction = if (transaction == null || transaction.created.isEmpty) None else Some(transaction)
      maybeTransaction
    }
    catch {
      case t: Throwable =>
        LOG.error(s"Unable to parse transaction from json $json", t)
        None
    }
  }
}
