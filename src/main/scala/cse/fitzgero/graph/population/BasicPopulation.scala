package cse.fitzgero.graph.population

import cse.fitzgero.graph.basicgraph.BasicGraph

import scala.collection.GenSeq
;

trait BasicPopulation { pop =>
  type EdgeId
  type VertexId
  type Graph <: BasicGraph {
    type EdgeId = pop.EdgeId
    type VertexId = pop.VertexId
  }

  type Path
  type Request <: BasicRequest
  type Response <: BasicResponse {
    type Path = pop.Path
  }
  type PopulationConfig

  def generateRequests(graph: Graph, config: PopulationConfig): GenSeq[Request]
  def resolveRequest(req: Request, path: Path): Response
}
