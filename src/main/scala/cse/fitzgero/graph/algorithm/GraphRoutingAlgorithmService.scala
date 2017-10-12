package cse.fitzgero.graph.algorithm

import java.time.Instant

import cse.fitzgero.graph.basicgraph.{BasicGraph, BasicOD}

import scala.concurrent.Future

trait GraphRoutingAlgorithmService { service =>
  val startTime: Long = Instant.now.toEpochMilli
  type VertexId
  type EdgeId
  type Graph <: BasicGraph {
    type VertexId = service.VertexId
    type EdgeId = service.EdgeId
  }
  type OD <: BasicOD
  type LoggingClass
  type ServiceResult <: { def logs: LoggingClass }
  type ServiceConfig <: Any

  /**
    * convenience function provided to calculate total service runtime
    * @return time in milliseconds
    */
  protected def runTime: Long = Instant.now.toEpochMilli - startTime

  /**
    * run the graph routing algorithm service as a future
    * @param graph underlying graph structure
    * @param request a signle request or a batch request
    * @param config a config object for the algorithm, defined by the implementation
    * @return a future resolving to an optional service result
    */
  def runService(graph: Graph, request: OD, config: Option[ServiceConfig]): Future[Option[ServiceResult]]
}