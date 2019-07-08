package ai.styx.frameworks.cassandra

import java.net.URI

import com.datastax.driver.core._

/**
  * This tool is a tester for an extremely simple Cassandra connector, just to check connection (url, ports) and try out some queries
  */
object CassandraTester {
  def main(args: Array[String]): Unit = {

    def connect(url: String, keyspace: String): Session = {
      // set up Cassandra session
      val uri = new URI("cassandra://" + url)
      val cluster = new Cluster.Builder()
        .addContactPoints(uri.getHost)
        .withPort(uri.getPort).withQueryOptions(new QueryOptions()
        .setConsistencyLevel(QueryOptions.DEFAULT_CONSISTENCY_LEVEL)).build

      // connect to the keyspace
      val session = cluster.connect
      session.execute("USE " + keyspace)
      session
    }

    println("Enter Cassandra url, e.g. 'localhost:9042':")
    val url = scala.io.StdIn.readLine()

    println("Enter keyspace:")
    val keyspace = scala.io.StdIn.readLine()

    println("Connecting...")
    val s = connect(url, keyspace)

    while (true) {
      println("Enter query:")
      val query = scala.io.StdIn.readLine()
      val results = List(s.execute(query))

      if (results.isEmpty) println("No records found.")
      else {
        println(s"Query executed succesfully. Found ${results.size} records. First 10:")
        for (result <- results.take(10)) {
          println(result.toString)
        }
      }
    }
  }
}