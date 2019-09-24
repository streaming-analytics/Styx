package ai.styx.frameworks.ignite

import org.apache.ignite.cache.CacheKeyConfiguration
import org.apache.ignite.configuration.IgniteConfiguration
import org.apache.ignite.{Ignite, Ignition}

import scala.io.Source

// extend this to start a local Ignite cluster on 127.0.0.1 for unit testing
trait EmbeddedIgnite {
//  val cfg = new IgniteConfiguration()
  //cfg.setCacheKeyConfiguration(new CacheKeyConfiguration("text", "id"))

   val ignite: Ignite = Ignition.start()
  // val ignite: Ignite = Ignition.start(getClass.getResource("/embedded-ignite-config.xml").getPath)
}
