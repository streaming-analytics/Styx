package com.styx.interfaces.repository

import com.styx.domain.CepDefinition

trait CepDefinitionRepository {

  /**
    * Get all relevant CEP Engine definitions for a raw event
    * @param event The type of raw event, e.g. 'UpdateCardBalance' or 'LocationUpdate'
    */
  def getCepDefinitions(event: String) : Seq[CepDefinition]
}
