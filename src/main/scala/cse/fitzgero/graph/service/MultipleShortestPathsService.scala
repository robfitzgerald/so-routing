package cse.fitzgero.graph.service

import cse.fitzgero.graph.algorithm.MultipleShortestPathsAlgorithm

import scala.collection.GenSeq
import scala.concurrent.Future

trait MultipleShortestPathsService extends GraphAlgorithmService {
//  type MultipleShortestPathsResult
//  abstract class MultipleShortestPathServiceResult {
//    def logs: LoggingClass
//    def result: MultipleShortestPathsResult
//  }
  type MultipleShortestPathServiceResult
  def runService(graph: Graph, odPairs: GenSeq[ODPair]): Future[Option[MultipleShortestPathServiceResult]]
}
