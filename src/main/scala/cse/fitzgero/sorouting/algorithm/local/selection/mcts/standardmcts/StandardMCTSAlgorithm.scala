package cse.fitzgero.sorouting.algorithm.local.selection.mcts.standardmcts

import scala.collection.{GenMap, GenSeq}

import cse.fitzgero.graph.algorithm.GraphAlgorithm
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.algorithm.local.selection.mcts._
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

  // helpers to recognize trivial reward averages
  // TODO: replace with provided reward average bounds
  def isApproximatelyOne(n: Double): Boolean = n <= 1 && n >= 0.999999D
  def isApproximatelyZero(n: Double): Boolean = n >= 0 && n <= 0.000001D

  override def runAlgorithm(graph: LocalGraph, request: GenMap[LocalODPair, GenSeq[Path]], config: Option[AlgorithmConfig]): Option[GenMap[LocalODPair, Path]] = {
    if (SORoutingPathSegment.hasNoOverlap(request.values.toSeq)) {
      println("[mCTS02] request was found to have no overlapping edges. reverting to greedy solution.")
      None
    } else {

      val solver = config match {
        case Some(conf) =>
          MCTSGlobalCongestionSolver02(
            graph = graph,
            request = request,
            seed = conf.randomSeed,
            duration = conf.computationalLimit,
            congestionThreshold = conf.congestionRatioThreshold
          )
        case None =>
          MCTSGlobalCongestionSolver02(
            graph = graph,
            request = request,
            congestionThreshold = 1.1D
          )
      }

      val tree = solver.run()

      if (tree.visits == 0) {
        println("[MCTS02] failed to produce a tree with the given problem.")
        None
      } else {
        val finalReward = tree.reward / tree.visits
        if (isApproximatelyOne(finalReward)) {
          println(f"[MCTS02] final reward average was ${finalReward * 100}%.2f%% (${tree.reward}/${tree.visits}) for root: trivial optimization. reverting to greedy solution.")
          None
        } else if (isApproximatelyZero(finalReward)) {
          println(f"[MCTS02] final reward average was ${finalReward * 100}%.2f%% (${tree.reward}/${tree.visits}) for root: impossible optimization. reverting to greedy solution.")
          None
        } else {
          val solution = solver.bestGame(tree)
          println(tree.printTree(1))
          Some(solver.unTag(solution))
        }
      }
    }
  }
}
