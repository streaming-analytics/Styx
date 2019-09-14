package ai.styx.app.spark

import ai.styx.common.Logging
import ai.styx.domain.utils.{Column, ColumnType}
import ai.styx.frameworks.ignite.IgniteFactory
import ai.styx.frameworks.interfaces.DatabaseWriter
import org.apache.ignite.{Ignite, Ignition}

object StyxFraudDetectionJob extends App with Logging {

  // test 1 : create an Ignite table on a local persistence store

  val dbFactory: IgniteFactory = new IgniteFactory("jdbc:ignite:thin://127.0.0.1")
  val dbWriter: DatabaseWriter = dbFactory.createWriter

  LOG.info("Starting...")

  var ignite: Ignite = Ignition.start()
  ignite.cluster().active(true)

  dbWriter.createTable("Table1", None, Some(List(Column("Column1", ColumnType.TEXT))))

  LOG.info("Done.")
}
