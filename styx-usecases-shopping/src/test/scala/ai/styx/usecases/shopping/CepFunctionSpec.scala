package ai.styx.usecases.shopping

import ai.styx.common.BaseSpec
import ai.styx.domain.events.BaseTransactionEvent
import org.joda.time.DateTime

class CepFunctionSpec extends BaseSpec {

  "Shopping CEP Function" should "do something" in {
    val cep = new CepFunction

    val t = BaseTransactionEvent("topic1", DateTime.now.toString("yyyyMMdd:HHmmss"), 21, "test1", 100.0, Map("key" -> "value"))

    val b = cep.createBusinessEvent(t)
    b should not be null
  }
}
