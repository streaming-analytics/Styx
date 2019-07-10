package ai.styx.domain.events

import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Tweet (
  //@JsonProperty("name") name: String = null,
  @JsonProperty("created_at") creationDate: String = null,
  @JsonProperty("text") messageText: String = null
  // no other fields needed for now
)