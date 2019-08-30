package ai.styx.frameworks.cassandra

import ai.styx.common.Logging
import com.datastax.driver.core.{Cluster, Session}

class CassandraConnector extends Logging {
  private var connected: Boolean = false
  private var cluster: Cluster = _
  private var session: Session = _

  def connect(node: String, port: Option[Int] = None, keyspace: String = "hypstar"): Unit = {
    try {
      LOG.info("Connecting to Cassandra...")
      val builder = Cluster.builder().addContactPoint(node)
      if (port.isDefined) builder.withPort(port.get)
      cluster = builder.build()
      session = cluster.connect(keyspace)
      connected = true
      LOG.info(s"Connected to Cassandra keyspace $keyspace on node $node")
    }
    catch {
      case t: Throwable =>
        LOG.error("ERROR while connecting to Cassandra: " + t.getMessage, t)
    }
  }

  def getSession: Session = {
    LOG.info("Getting Cassandra session...")
    if (!connected) throw new Exception("Make a connection first")
    session
  }

  def close(): Unit = {
    LOG.info("Closing Cassandra session...")
    if (!connected) throw new Exception("Make a connection first")
    session.close()
    cluster.close()
    connected = false
  }
}
