package com.styx.frameworks.cassandra

import com.styx.common.Logging
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{BeforeAndAfterAll, Suite}

trait EmbeddedCassandra extends BeforeAndAfterAll with Logging {
  this: Suite =>

  override def beforeAll(): Unit = {
    super.beforeAll()

    logger.info("Starting embedded Cassandra...")
    EmbeddedCassandraServerHelper.startEmbeddedCassandra()
  }
}
