package ai.styx.frameworks.ignite

import java.sql.DriverManager

import ai.styx.common.Logging
import org.apache.ignite.{Ignite, Ignition}

object IgniteConnector extends Logging {

  //var ignite: Ignite = Ignition.start()

  //Class.forName("org.apache.ignite.IgniteJdbcThinDriver")
  //val conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1")

//  def createDatabaseTables(): Unit = {
//    val sql = conn.createStatement
//    sql.executeUpdate("CREATE TABLE Employee (" + " id INTEGER PRIMARY KEY, name VARCHAR, isEmployed tinyint(1)) WITH \"template=replicated\"")
//    sql.executeUpdate("CREATE INDEX idx_employee_name ON Employee (name)")
//  }
//
//  def insertData() = {
//    val sql = conn.prepareStatement("INSERT INTO Employee (id, name, isEmployed) VALUES (?, ?, ?);")
//    sql.setLong(1, 1)
//    sql.setString(2, "Bas")
//    sql.setBoolean(3, true)
//    sql.executeUpdate()
//  }
//
//  def getData() = {
//    val sql = conn.createStatement()
//    val rs = sql.executeQuery("SELECT e.name, e.isEmployed FROM Employee e WHERE e.isEmployed = TRUE;")
//
//    while (rs.next()) {
//      LOG.info(rs.getString(1) + ", " + rs.getString(2))
//    }
//  }
}
