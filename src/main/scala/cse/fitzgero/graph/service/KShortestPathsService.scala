package cse.fitzgero.graph.service

import cse.fitzgero.graph.algorithm.GraphAlgorithm

import scala.concurrent.Future

trait KShortestPathsService extends GraphAlgorithmService {
  def runService(graph: Graph, oDPair: ODPair): Future[Option[ServiceResult]]
}
