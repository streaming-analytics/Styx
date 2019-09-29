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
case class Tweet (
                   @BeanProperty
                   @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EE MMM dd HH:mm:ss Z yyyy", locale = "en")
                   @JsonProperty("created_at")
                   @JsonDeserialize(using = classOf[TimestampDeserializer])
                   created_at: Timestamp = null,

                   @BeanProperty
                   @JsonProperty("text")
                   text: String = null
                   // no other fields needed for now
) {
  def created: Option[DateTime] = {
    try {
      Some(new DateTime(created_at))
    }
    catch {
      case t: Throwable => None
    }
  }
  def toJson(): String = Tweet.objectMapper.writeValueAsString(this)
}

object Tweet extends Logging {
  private val mapper: ObjectMapper = new ObjectMapper()
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
  mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)

  def objectMapper = mapper

  def fromString(s: String): Tweet = {
    try {
      val tweet = mapper.readValue(s, classOf[Tweet]) // .replaceAll("[$\\[\\]{}]", "")
      if (tweet == null || tweet.text == null || tweet.created_at == null) null else tweet
    }
    catch {
      case t: Throwable =>
        LOG.error(s"Unable to parse tweet from string $s", t)
        null
    }
  }

  def fromJson(json: String): Option[Tweet] = {
    try {
      val tweet = mapper.readValue(json, classOf[Tweet]) // .replaceAll("[$\\[\\]{}]", "")
      val maybeTweet = if (tweet == null || tweet.text == null || tweet.created_at == null) None else Some(tweet)
      maybeTweet
    }
    catch {
      case t: Throwable =>
        LOG.error(s"Unable to parse tweet from json $json", t)
        None
    }
  }
}

case class TweetWord(created_at: Timestamp, word: String)

case class TweetWindowTrend(id: String, windowPart: Int, windowStart: Timestamp, windowEnd: Timestamp, word: String, count: Long) extends DomainEntity(id)
