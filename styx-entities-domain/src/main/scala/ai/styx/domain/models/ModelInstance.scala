package ai.styx.domain.models

import ai.styx.domain.{Customer, PmmlModel}

trait ModelInstance {
  def score(customer: Customer): Double
  def toPmml : PmmlModel
}
