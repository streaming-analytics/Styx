package com.styx.domain

import com.styx.domain.models.ModelInstance

abstract class Attributes

// the attributes / characteristics of a customer
case class Balance(card_id: String, acc_num: Int, balance: Double, card_type: String) extends Attributes
case class CepDefinition(Name: String, RawEvent: String, BusinessEvent: String, Criteria: String, Model: String) extends Attributes
case class InitialBalance(AccountNumber: Int, CardId: String, Balance: Double) extends Attributes
case class CriteriaDefinition(Name: String, Event: String, Criteria: String) extends Attributes

// the attributes / characteristics of a notification
case class NotificationFilter(Name: String, Event: String, Model: ModelInstance, Threshold: Double, Message: String) extends Attributes
case class NotificationDefinition(Name: String, Event: String, Model: String, Threshold: Double, Message: String) extends Attributes

// PMML models
case class PmmlModel(Name: String, Pmml: String) extends Attributes
