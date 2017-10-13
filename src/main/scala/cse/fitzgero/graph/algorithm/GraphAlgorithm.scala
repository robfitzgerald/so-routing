package cse.fitzgero.graph.algorithm

import cse.fitzgero.graph.basicgraph._

trait GraphAlgorithm { algorithm =>
  // the algorithm should use the same type for VertexId and EdgeId as the underlying graph
  type VertexId
  type EdgeId
  type Graph <: BasicGraph {
    type VertexId = algorithm.VertexId
    type EdgeId = algorithm.EdgeId
  }

  type AlgorithmRequest
  type AlgorithmResult
  type AlgorithmConfig <: Any

  def runAlgorithm(graph: Graph, request: AlgorithmRequest, config: Option[AlgorithmConfig]): Option[AlgorithmResult]
}
