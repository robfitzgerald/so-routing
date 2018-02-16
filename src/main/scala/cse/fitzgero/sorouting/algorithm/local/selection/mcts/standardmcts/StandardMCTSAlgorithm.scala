package cse.fitzgero.sorouting.algorithm.local.selection.mcts.standardmcts

import scala.collection.{GenMap, GenSeq}

import cse.fitzgero.graph.algorithm.GraphAlgorithm
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.algorithm.local.selection.mcts.MCTSSolver
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

object StandardMCTSAlgorithm extends GraphAlgorithm {
  /////// reference types
  override type VertexId = KSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = KSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = KSPLocalDijkstrasAlgorithm.Graph
  type Path = List[SORoutingPathSegment]

  /////// algorithm API
  override type AlgorithmRequest = GenMap[LocalODPair, GenSeq[Path]]
  override type AlgorithmConfig = {
    def congestionRatioThreshold: Double // used to find "congestion improvement" in search
    def randomSeed: Long
    def computationalLimit: Long // ms. spent searching before decision
  }
  type AlgorithmResult = GenMap[LocalODPair, Path]

  override def runAlgorithm(graph: LocalGraph, request: GenMap[LocalODPair, GenSeq[Path]], config: Option[AlgorithmConfig]): Option[GenMap[LocalODPair, Path]] = {
    if (SORoutingPathSegment.hasNoOverlap(request.values.toSeq)) {
      println("[mCTS02] request was found to have no overlapping edges. reverting to greedy solution.")
      None
    } else {
      val solver = config match {
        case Some(conf) =>
          MCTSSolver(
            graph = graph,
            request = request,
            seed = conf.randomSeed,
            duration = conf.computationalLimit,
            congestionThreshold = conf.congestionRatioThreshold
          )
        case None =>
          MCTSSolver(
            graph = graph,
            request = request,
            congestionThreshold = 1.1D
          )
      }
      val tree = solver.run()
      val finalReward = tree.reward / tree.visits
      if (finalReward >= 0.99D) {
        println("[MCTS02] final reward greater than 99% for root: trivial optimization. reverting to greedy solution.")
        None
      } else if (finalReward < 0.01D) {
        println("[MCTS02] final reward less than 1% for root: impossible optimization. reverting to greedy solution.")
        None
      } else {
        val solution = solver.bestGame(tree)
        println(tree.printTree(1))
        Some(solver.unTag(solution))
      }
    }
  }
}
