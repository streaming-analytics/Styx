package com.styx.interfaces.repository

import com.styx.domain.CepDefinition

import scala.collection.parallel.mutable

class MemoryCepDefinitionRepository extends CepDefinitionRepository {
  val models = mutable.ParHashMap[String, Seq[CepDefinition]]()
  def getCepDefinitions(event: String): Seq[CepDefinition] =
      models.getOrElse(event, Seq.empty)
}
