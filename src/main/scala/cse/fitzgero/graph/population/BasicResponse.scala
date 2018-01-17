package cse.fitzgero.graph.population

import cse.fitzgero.graph.basicgraph.BasicPathSegment

trait BasicResponse {
  type Request <: BasicRequest
  type Path <: Seq[BasicPathSegment]
  def request: Request
  def path: Path
}
