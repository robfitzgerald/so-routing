package cse.fitzgero.sorouting.model.population

import java.time.LocalTime

import cse.fitzgero.graph.population.BasicRequest

trait SORoutingRequest extends BasicRequest {
  override type TimeUnit = LocalTime
  override type Id = String
}
