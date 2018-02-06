package com.styx.setup

import com.styx.interfaces.repository._
import com.typesafe.config.Config

trait RepositoryFactory extends Serializable {
  def createRepositoryInstances(styxConfig: Config): RepositoryInstances
  def createCustomerProfileRepository(styxConfig: Config): CustomerProfileRepository
  def createCriteriaFilterRepository(styxConfig: Config): CriteriaFilterRepository
  def createNotificationFilterRepository(styxConfig: Config): NotificationFilterRepository
}
