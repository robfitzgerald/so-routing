package cse.fitzgero.graph.service

import cse.fitzgero.graph.algorithm.ShortestPathAlgorithm

import scala.concurrent.Future

trait ShortestPathService extends GraphAlgorithmService {
  type ShortestPathResult <: ShortestPathAlgorithm#ShortestPathResult
  abstract class ShortestPathServiceResult {
    def logs: LoggingClass
    def result: ShortestPathResult
  }
  def runService(graph: Graph, oDPair: ODPair): Future[Option[ShortestPathServiceResult]]
}