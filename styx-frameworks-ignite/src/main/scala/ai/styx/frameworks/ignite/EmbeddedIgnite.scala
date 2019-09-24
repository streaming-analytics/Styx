package ai.styx.frameworks.ignite

import org.apache.ignite.{Ignite, Ignition}

// extend this to start a local Ignite cluster on 127.0.0.1 for unit testing
trait EmbeddedIgnite {
  var ignite: Ignite = Ignition.start()
}
