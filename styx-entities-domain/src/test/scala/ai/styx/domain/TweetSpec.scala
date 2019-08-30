package ai.styx.domain

import ai.styx.common.BaseSpec
import ai.styx.domain.events.Tweet

class TweetSpec extends BaseSpec {
  "Tweet" should "serialize to JSON" in {
    val tweet = Tweet("Thu Jul 18 20:17:04 -0700 2019", "This is a test tweet")
    val json = tweet.toJson()
    assert(json.contains("This is a test tweet"))
  }

  it should "deserialize from JSON" in {
    val json = "{\"created_at\":\"Fri Jul 19 14:12:04 -0700 2019\",\"messageText\":\"This is another test tweet\"}"
    val tweet = Tweet.fromJson(json)
    tweet.isDefined shouldBe true
    tweet.get.created.get.dayOfMonth().get() shouldBe 19
    tweet.get.messageText shouldBe "This is another test tweet"
  }

  it should "parse a datetime string" in {
    val tweet = Tweet("Wed Jul 17 11:17:31 -0800 2019", "This is a test tweet")
    tweet.created.isDefined shouldBe true
    tweet.created.get.year().get shouldBe 2019

    val tweet2 = Tweet("This is not a valid date", "This is a test tweet")
    tweet2.created_at shouldBe "This is not a valid date"
    tweet2.created.isDefined shouldBe false
  }
}
