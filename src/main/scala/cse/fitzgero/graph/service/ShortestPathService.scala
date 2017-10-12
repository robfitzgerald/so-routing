package cse.fitzgero.graph.service

import cse.fitzgero.graph.algorithm.ShortestPathAlgorithm

import scala.concurrent.Future

trait ShortestPathService extends GraphAlgorithmService {
  def runService(graph: Graph, oDPair: ODPair): Future[Option[ServiceResult]]
}