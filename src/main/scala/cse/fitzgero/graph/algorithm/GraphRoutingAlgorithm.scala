package cse.fitzgero.graph.algorithm

import cse.fitzgero.graph.basicgraph._

trait GraphRoutingAlgorithm { algorithm =>
  // the algorithm should use the same type for VertexId and EdgeId as the underlying graph
  type VertexId
  type EdgeId
  type Graph <: BasicGraph {
    type VertexId = algorithm.VertexId
    type EdgeId = algorithm.EdgeId
  }
  type RequestId
  type OD <: BasicOD
  type PathSegment
  type Path = Seq[PathSegment]
  type AlgorithmResult
  type AlgorithmConfig <: Any

  /**
    * run a graph routing algorithm in the current process
    * @param graph the underlying graph structure
    * @param request a single request or a batch request
    * @return the optional algorithm result
    */
  def runAlgorithm(graph: Graph, request: OD, config: Option[AlgorithmConfig]): Option[AlgorithmResult]
}