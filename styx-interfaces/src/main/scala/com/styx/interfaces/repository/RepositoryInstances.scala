package com.styx.interfaces.repository

case class RepositoryInstances (
                                 customerProfileRepository: CustomerProfileRepository,
                                 criteriaFilterRepository: CriteriaFilterRepository,
                                 notificationFilterRepository: NotificationFilterRepository,
                                 CepDefinitionRepository: CepDefinitionRepository
                               )
