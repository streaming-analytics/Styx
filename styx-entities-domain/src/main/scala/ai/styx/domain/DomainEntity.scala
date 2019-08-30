package ai.styx.domain

abstract class DomainEntity(id: String) extends Entity {
  def getId: String = id
}