package cse.fitzgero.sorouting.model.population

import cse.fitzgero.graph.population.BasicResponse
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment

trait SORoutingResponse extends BasicResponse {
  type Request <: SORoutingRequest
  override type Path = Seq[SORoutingPathSegment]
}
