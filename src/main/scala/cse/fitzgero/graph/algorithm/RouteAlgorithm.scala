package cse.fitzgero.graph.algorithm

import scala.collection.{GenMap, GenSeq}

trait RouteAlgorithm extends GraphAlgorithm {
  abstract class RouteResult {
    def paths: GenMap[ODPair, Path]
  }
  def runAlgorithm(g: Graph, odPairs: GenSeq[ODPair]): Option[RouteResult]
}