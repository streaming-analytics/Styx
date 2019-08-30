package ai.styx.frameworks.interfaces

trait DatabaseFactory {
  def createFetcher: DatabaseFetcher
  def createWriter: DatabaseWriter
}
