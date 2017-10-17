package cse.fitzgero.graph.algorithm

import cse.fitzgero.graph.population.BasicRequest

import scala.collection.GenSeq
import scala.concurrent.Future

trait GraphBatchRoutingAlgorithmService extends GraphService { service =>
  type ServiceRequest <: GenSeq[BasicRequest]

  /**
    * run the graph routing algorithm service as a future
    * @param graph underlying graph structure
    * @param request a signle request or a batch request
    * @param config a config object for the algorithm, defined by the implementation
    * @return a future resolving to an optional service result
    */
  def runService(graph: Graph, request: ServiceRequest, config: Option[ServiceConfig]): Future[Option[ServiceResult]]
}