package cse.fitzgero.graph.algorithm

trait ShortestPathAlgorithm extends GraphAlgorithm {
  abstract class ShortestPathResult {
    def od: ODPair
    def path: Path
  }
  def runAlgorithm(g: Graph, od: ODPair): Option[ShortestPathResult]
}