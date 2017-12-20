package cse.fitzgero.sorouting.model.population

import cse.fitzgero.sorouting.model.path.SORoutingPathSegment

/**
  * a handled, routed request
  * @param request the original request
  * @param path the route provided to the request
  */
case class LocalResponse(request: LocalRequest, path: List[SORoutingPathSegment]) extends SORoutingResponse {
  override type Request = LocalRequest
}
