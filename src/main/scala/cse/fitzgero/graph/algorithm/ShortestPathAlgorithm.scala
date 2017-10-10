package cse.fitzgero.graph.algorithm

import scala.concurrent.Future

trait ShortestPathAlgorithm extends GraphAlgorithm {
  abstract class ShortestPathResult {
    def od: ODPair
    def path: Path
  }
  def runAlgorithm(g: Graph, od: ODPair): Option[ShortestPathResult]
}

trait ShortestPathService extends GraphAlgorithmService with ShortestPathAlgorithm {
  abstract class ShortestPathServiceResult {
    def logs: LoggingClass
    def result: ShortestPathResult
  }
  def runService(graph: Graph, oDPair: ODPair): Future[Option[ShortestPathServiceResult]]
}