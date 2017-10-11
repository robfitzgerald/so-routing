package cse.fitzgero.graph.algorithm

import scala.collection.GenSeq
import scala.concurrent.Future

import cse.fitzgero.graph.util.KSPBounds

trait KShortestPathsAlgorithm extends GraphAlgorithm {
  abstract class KShortestPathsResult {
    def od: ODPair
    def paths: GenSeq[Path]
    def k: Int
    def kspBounds: KSPBounds
  }
  /**
    * desired number of alternate paths, likely a constructor value for the derived KSP class
    */
  def k: Int

  /**
    * a configuration declaring how this instance of a KSP algorithm will halt, also likely a
    * constructor value for the derived KSP class
    */
  def kspBounds: KSPBounds
  def runAlgorithm(g: Graph, od: ODPair): Option[KShortestPathsResult]
}


trait KShortestPathsService extends GraphAlgorithmService with KShortestPathsAlgorithm {
  abstract class KShortestPathsServiceResult {
    def logs: LoggingClass
    def result: KShortestPathsResult
  }
  def runService(graph: Graph, oDPair: ODPair): Future[Option[KShortestPathsServiceResult]]
}