package cse.fitzgero.graph.algorithm

import scala.collection.{GenMap, GenSeq}

trait MultipleShortestPathsAlgorithm extends GraphAlgorithm {
  abstract class MultipleShortestPathsResult {
    def ods: GenMap[ODPair, Path]
  }
  def runAlgorithm(g: Graph, odPairs: GenSeq[ODPair]): Option[MultipleShortestPathsResult]
}

