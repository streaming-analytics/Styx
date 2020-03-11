package ai.styx.frameworks.cassandra

import java.net.InetSocketAddress

import ai.styx.common.Logging
import com.datastax.oss.driver.api.core.session.Session
import com.datastax.oss.driver.api.core.CqlSession

class CassandraConnector extends Logging {
  private var connected: Boolean = false
  //private var cluster: Cluster = _
  private var session: CqlSession = _

  def connect(node: String, port: Option[Int] = None, keyspace: String = "Styx"): Unit = {
    try {
      LOG.info("Connecting to Cassandra...")
      val endPoint = InetSocketAddress.createUnresolved(node, port.getOrElse(9042))
      val builder = CqlSession.builder().addContactPoint(endPoint)
      //val builder = Cluster.builder().addContactPoint(node)
      //cluster = builder.build()
      session = builder.build() //cluster.connect(keyspace)
      connected = true
      LOG.info(s"Connected to Cassandra keyspace $keyspace on node $node")
    }
    catch {
      case t: Throwable =>
        LOG.error("ERROR while connecting to Cassandra: " + t.getMessage, t)
    }
  }

  def getSession: CqlSession = {
    LOG.info("Getting Cassandra session...")
    if (!connected) throw new Exception("Make a connection first")
    session
  }

  def close(): Unit = {
    LOG.info("Closing Cassandra session...")
    if (!connected) throw new Exception("Make a connection first")
    session.close()
    // cluster.close()
    connected = false
  }
}
