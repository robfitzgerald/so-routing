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
  type RequestId
  type ODPair <: BasicODPair[VertexId]
  case class PathSegment (e: EdgeId, cost: Option[Seq[Double]])
  type Path = Seq[PathSegment]
  type AlgorithmResult
}

trait GraphAlgorithmService { service =>
  type LoggingClass
  type ServiceResult <: { def logs: LoggingClass }
}