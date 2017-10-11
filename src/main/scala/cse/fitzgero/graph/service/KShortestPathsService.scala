package cse.fitzgero.graph.service

import cse.fitzgero.graph.algorithm.GraphAlgorithm

import scala.concurrent.Future

trait KShortestPathsService extends GraphAlgorithmService {
  type KShortestPathsResult <: GraphAlgorithm#AlgorithmResult
  abstract class KShortestPathsServiceResult {
    def logs: LoggingClass
    def result: KShortestPathsResult
  }
  def runService(graph: Graph, oDPair: ODPair): Future[Option[KShortestPathsServiceResult]]
}
