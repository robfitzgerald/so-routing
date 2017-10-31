package cse.fitzgero.graph.algorithm

import java.time.Instant

import cse.fitzgero.graph.basicgraph._

import scala.concurrent.Future

trait GraphService { service =>
  // the service should use the same type for VertexId and EdgeId as the underlying graph
  type VertexId
  type EdgeId
  type Graph <: BasicGraph {
    type VertexId = service.VertexId
    type EdgeId = service.EdgeId
  }

  val startTime: Long = Instant.now.toEpochMilli

  /**
    * convenience function provided to calculate total service runtime
    * @return time in milliseconds
    */
  protected def runTime: Long = Instant.now.toEpochMilli - startTime

  type LoggingClass
  type ServiceConfig <: Any
  type ServiceRequest
  type ServiceResult <: {
    def logs: LoggingClass
  }

  /**
    * run a graph routing service as a future
    * @param graph underlying graph structure
    * @param request a single request or a batch request
    * @param config a config object for the algorithm, defined by the implementation
    * @return a future resolving to an optional service result
    */
  def runService(graph: Graph, request: ServiceRequest, config: Option[ServiceConfig]): Future[Option[ServiceResult]]
}
