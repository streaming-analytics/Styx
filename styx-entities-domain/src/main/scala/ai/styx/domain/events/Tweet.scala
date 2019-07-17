package ai.styx.domain.events

import java.util.Locale

import ai.styx.common.Logging
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import scala.beans.BeanProperty

@JsonIgnoreProperties(ignoreUnknown = true)
case class Tweet (
                   //@JsonProperty("name") name: String = null,
                   @BeanProperty @JsonProperty("created_at") created_at: String = null,
                   @BeanProperty @JsonProperty("text") messageText: String = null
                   // no other fields needed for now
) {
  def created: Option[DateTime] =
    try {
      // format: Sat Sep 10 22:23:38 +0000 2011
      Some(DateTime.parse(created_at, DateTimeFormat.forPattern("EE MMM dd HH:mm:ss Z yyyy").withLocale(Locale.ENGLISH)))
    }
    catch {
      case _: Throwable => None
    }

  def toJson(mapper: ObjectMapper): String = mapper.writeValueAsString(this)
}

object Tweet extends Logging {
  val mapper: ObjectMapper = new ObjectMapper()
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
  mapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false)

  def parse(line: String): Option[Tweet] = {
    try {
      val tweet = mapper.readValue(line, classOf[Tweet]) // .replaceAll("[$\\[\\]{}]", "")
      //LOG.debug(s"Parsed tweet ${tweet.messageText}")
      val maybeTweet = if (tweet == null || tweet.messageText == null || tweet.created_at == null) None else Some(tweet)
      maybeTweet
    }
    catch {
      case t: Throwable =>
        LOG.error(s"Unable to parse tweet $line", t)
        None
    }
  }
}