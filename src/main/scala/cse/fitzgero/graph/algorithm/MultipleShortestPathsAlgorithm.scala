package cse.fitzgero.graph.algorithm

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.Future

trait MultipleShortestPathsAlgorithm extends GraphAlgorithm {
  abstract class MultipleShortestPathsResult {
    def ods: GenMap[ODPair, Path]
  }
  def runAlgorithm(g: Graph, odPairs: GenSeq[ODPair]): Option[MultipleShortestPathsResult]
}

trait MultipleShortestPathService extends GraphAlgorithmService with MultipleShortestPathsAlgorithm {
  abstract class MultipleShortestPathServiceResult {
    def logs: LoggingClass
    def result: MultipleShortestPathsResult
  }
  def runService(graph: Graph, oDPair: ODPair): Future[Option[MultipleShortestPathServiceResult]]
}