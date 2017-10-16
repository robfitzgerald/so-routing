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

  /**
    * run a graph algorithm with a generic set of requirements
    * @param graph underlying graph structure
    * @param request user-defined request object
    * @param config user-defined config object
    * @return a user-defined result object
    */
  def runAlgorithm(graph: Graph, request: AlgorithmRequest, config: Option[AlgorithmConfig]): Option[AlgorithmResult]
}
