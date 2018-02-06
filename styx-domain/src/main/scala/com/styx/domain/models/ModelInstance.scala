package com.styx.domain.models

import com.styx.domain.{Customer, PmmlModel}

trait ModelInstance {
  def score(customer: Customer): Double
  def toPmml : PmmlModel
}
