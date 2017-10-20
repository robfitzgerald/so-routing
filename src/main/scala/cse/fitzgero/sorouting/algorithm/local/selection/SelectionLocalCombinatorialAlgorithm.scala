package cse.fitzgero.sorouting.algorithm.local.selection

import cse.fitzgero.graph.algorithm.GraphAlgorithm
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

import scala.collection.{GenMap, GenSeq}

object SelectionLocalCombinatorialAlgorithm extends GraphAlgorithm {
  override type VertexId = KSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = KSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = KSPLocalDijkstrasAlgorithm.Graph
  type Path = List[SORoutingPathSegment]
  override type AlgorithmRequest = GenMap[LocalODPair, GenSeq[Path]]
  type SSSPAlgorithmResult = KSPLocalDijkstrasAlgorithm.AlgorithmResult
  override type AlgorithmConfig = Nothing

  val DefaultFlowCost: Double = 0D
  val ParallelizationDepth: Int = 2 // produces k ^ ParallelizationDepth lightweight threads in this computation

  type AlgorithmResult = GenMap[LocalODPair, Path]

  /**
    * run the naive combinatorial optimization algorithm, returning the optimal route combination for this set of alternates
    * @param graph underlying graph structure
    * @param request a map of request objects to their sets of alternate paths as found by an alternate paths solver
    * @param config (unused)
    * @return a map of request objects to their optimal paths, with one path per request
    */
  override def runAlgorithm(graph: Graph, request: GenMap[LocalODPair, GenSeq[Path]], config: Option[Nothing] = None): Option[AlgorithmResult] = {

    case class Tag(personId: String, alternate: Int)

    // helper for inspecting a path cost
    def pathCost(p: Path): Double =
      p.map(_.cost.getOrElse(List[Double]()).sum).sum

    // a back-tracking map from personIds to their OD object
    val personToODPair: GenMap[String, LocalODPair] =
      request.keys.map(od => (od.id, od)).toMap

    // a multiset of path sets for each person
    val tagsAndPaths: GenSeq[GenSeq[(Tag, Path)]] =
      request.map(od => {
        od._2.zipWithIndex
          .map(path => {
            (Tag(od._1.id, path._2), path._1)
          })
      }).toSeq

    // combinatorial solver
    def minimalMultisetCombinationsOf(multiset: GenSeq[GenSeq[(Tag, Path)]]): Option[GenSeq[(Tag, Path)]] = {

      def _mmC(subSet: GenSeq[GenSeq[(Tag, Path)]], thisCombination: GenSeq[(Tag, Path)] = GenSeq(), depth: Int = 1): GenSeq[(Double, GenSeq[(Tag, Path)])] = {
        if (subSet.isEmpty) {
          // take all edges out of this combination, attach flow count values
          val edgesVisited: GenMap[EdgeId, Int] =
            thisCombination
              .flatMap(_._2.map(edge => (edge.edgeId, 1)))
              .groupBy(_._1)
              .mapValues(_.map(_._2).sum)

          // calculate cost of added flow for each named edge
          val addedCost: Double =
            edgesVisited.map(edgeAndFlow => {
              graph.edgeById(edgeAndFlow._1) match {
                case None =>
                  println(s"[SelectionLocal] Edge (id, flow): $edgeAndFlow does not correspond to an edge in the original graph")
                  0D
                case Some(edge) =>
                  edge.attribute
                    .costFlow(edgeAndFlow._2)
                    .getOrElse(DefaultFlowCost) // TODO: add policy for managing missing cost flow evaluation data (is zero the correct default value?)
              }
            }).sum

          // sum and return with a calculated cost as a single-item GenSeq[] with one tuple with one cost and a GenSeq[] as long as the # of buckets
          GenSeq((addedCost, thisCombination))

        } else {
          // parallelize subproblems up to a defined depth
          val thisBucket: GenSeq[(Tag, Path)] =
            if (depth <= ParallelizationDepth) subSet.head.par
            else subSet.head

          thisBucket.map(item => {
            val combinationsOnThisBranch = _mmC(subSet.tail, item +: thisCombination, depth + 1)
            combinationsOnThisBranch.minBy(_._1)
          })
        }
      }

      if (multiset.isEmpty) None
      else {
        // sorting by descending order so we can better reason about the parallelization occurring
        val sortedBySizeDescending = multiset.toVector.sortBy(-_.size)
        val result = _mmC(sortedBySizeDescending)
        Some(result.minBy(_._1)._2)
      }
    }

    minimalMultisetCombinationsOf(tagsAndPaths) match {
      case None => None
      case Some(combination) =>
        val result: GenMap[LocalODPair, Path] = combination
          .map(tagAndPath => {
            (personToODPair(tagAndPath._1.personId), tagAndPath._2)
          }).toMap

        Some(result)
    }
  }
}
