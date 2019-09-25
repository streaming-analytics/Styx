package ai.styx.frameworks.ignite

import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.{Ignite, Ignition}

/**
  * Extend this to start a local Ignite cluster on 127.0.0.1 for unit testing
  */
trait EmbeddedIgnite {
  val cfg = new IgniteConfiguration()

  // will connect to a local Ignite, or will start a temporary local cluster
  val ignite: Ignite = Ignition.getOrStart(cfg) //.start()

  // alternative: specify the configuration in an xml file
  // val ignite: Ignite = Ignition.start(getClass.getResource("/embedded-ignite-config.xml").getPath)
}
