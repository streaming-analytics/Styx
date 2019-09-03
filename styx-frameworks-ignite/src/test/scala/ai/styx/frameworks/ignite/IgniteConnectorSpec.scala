package ai.styx.frameworks.ignite

import ai.styx.common.BaseSpec

class IgniteConnectorSpec extends BaseSpec with EmbeddedIgnite {

  "Ignite Connector" should "start and connect to a local database" in {
    // IgniteConnector.createDatabaseTables()
    LOG.info(ignite.configuration().getIgniteHome)
  }

  it should "write and fetch some data" in {
    //IgniteConnector.insertData()
    //IgniteConnector.getData()
  }
}
