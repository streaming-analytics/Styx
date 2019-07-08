package ai.styx.frameworks.cassandra

import ai.styx.common.BaseSpec
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

class CassandraSpec extends BaseSpec with EmbeddedCassandra {

  "Cassandra" should "start local embedded server" in {
    EmbeddedCassandraServerHelper.getHost shouldBe "localhost"
  }
}
