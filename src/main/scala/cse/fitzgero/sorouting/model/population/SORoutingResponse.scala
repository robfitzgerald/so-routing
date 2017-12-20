package cse.fitzgero.sorouting.model.population

import cse.fitzgero.graph.population.BasicResponse
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment

/**
  * the model-agnostic routing request response
  */
trait SORoutingResponse extends BasicResponse {
  type Request <: SORoutingRequest
  override type Path = Seq[SORoutingPathSegment]
}
