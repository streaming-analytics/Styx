package ai.styx.domain

import java.sql.Timestamp

import ai.styx.common.BaseSpec
import ai.styx.domain.events.Tweet

class TweetSpec extends BaseSpec {
  "Tweet" should "serialize to JSON" in {
    val tweet = Tweet(new Timestamp(2019, 7, 18, 20, 17, 4, 0), "This is a test tweet")
    val json = tweet.toJson()
    assert(json.contains("This is a test tweet"))
  }

  it should "deserialize from JSON" in {
    val json = "{\"created_at\":\"Fri Jul 19 14:12:04 -0700 2019\",\"text\":\"This is another test tweet\"}"
    val tweet = Tweet.fromJson(json)
    tweet.isDefined shouldBe true
    tweet.get.created.get.year().get() shouldBe 2019
    tweet.get.created.get.dayOfMonth().get() shouldBe 19
    tweet.get.text shouldBe "This is another test tweet"
  }
}
