package cse.fitzgero.graph.algorithm

import scala.collection.GenSeq

trait PathSelectionAlgorithm extends GraphAlgorithm {
  abstract class PathSelectionResult {
    def originalPaths: GenSeq[Path]
    def selectedPaths: GenSeq[Path]
  }
  def runAlgorithm(g: Graph, paths: GenSeq[Path]): Option[PathSelectionResult]
}