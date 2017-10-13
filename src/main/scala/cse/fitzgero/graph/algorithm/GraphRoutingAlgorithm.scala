package cse.fitzgero.graph.algorithm

import cse.fitzgero.graph.basicgraph._

trait GraphRoutingAlgorithm extends GraphAlgorithm { algorithm =>
  type AlgorithmRequest <: BasicOD
  type PathSegment
  type Path = Seq[PathSegment]

  /**
    * run a graph routing algorithm in the current process
    * @param graph the underlying graph structure
    * @param request a single request or a batch request
    * @return the optional algorithm result
    */
  def runAlgorithm(graph: Graph, request: AlgorithmRequest, config: Option[AlgorithmConfig]): Option[AlgorithmResult]
}