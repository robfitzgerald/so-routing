package cse.fitzgero.sorouting.model.population

import cse.fitzgero.sorouting.model.path.SORoutingPathSegment

case class LocalResponse(request: LocalRequest, path: List[SORoutingPathSegment]) extends SORoutingResponse {
  override type Request = LocalRequest
}
