package ai.styx.frameworks.ignite

import ai.styx.common.BaseSpec

class IgniteConnectorSpec extends BaseSpec {

  "Ignite Connector" should "connect to a local database and create a table" in {
    IgniteConnector.createDatabaseTables()
  }

  it should "write and fetch some data" in {
    IgniteConnector.insertData()
    IgniteConnector.getData()
  }
}
