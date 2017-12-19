package cse.fitzgero.sorouting.algorithm.local.selection

import scala.annotation.tailrec

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
    if (request.isEmpty) {
      None
    } else {
      case class Tag(personId: String, alternate: Int)

      type AltIndices = Vector[Int]

      // a back-tracking map from personIds to their OD object
      val unTag: GenMap[String, LocalODPair] =
        request.keys.map(od => (od.id, od)).toMap



      def solve(): GenSeq[(Tag, Path)] = {
        val globalAlternates: Vector[Vector[(Tag, Path)]] =
          request.map { od => {
            od._2.toVector.zipWithIndex
              .map(path => {
                (Tag(od._1.id, path._2), path._1)
              })
          }
          }.toVector

        val startIndices: AltIndices = globalAlternates.map { _ => 0 }
        val finalIndices: AltIndices = globalAlternates.map { _.size - 1 }

//        def atEnd(ind: AltIndices): Boolean = ind == finalIndices

        // invariant: starts from last position
        def advance(startIndices: AltIndices): Option[AltIndices] = {
          if (startIndices == finalIndices) { None }
          else if (startIndices.isEmpty) { None }
          else {
            @tailrec
            def _advance (ind: AltIndices, currentBucket: Int) : AltIndices = {
              if (ind(currentBucket) < globalAlternates(currentBucket).size - 1) {
                ind.updated(currentBucket, ind(currentBucket) + 1)
              } else {
                if (currentBucket == 0) {
                  ind
                } else {
                  _advance(ind.updated(currentBucket, 0), currentBucket - 1)
                }
              }
            }
            Some {
              _advance(startIndices, startIndices.size - 1)
            }
          }
        }

        def alternatesAt(ind: AltIndices): GenSeq[(Tag, Path)] =
          ind.zipWithIndex
            .map { i =>
              globalAlternates(i._2)(i._1)
            }

        def evaluate(thisCombination: GenSeq[(Tag, Path)]): Double = {
          val edgesVisited: GenMap[EdgeId, Int] =
            thisCombination
              .flatMap(_._2.map(edge => (edge.edgeId, 1)))
              .groupBy(_._1)
              .mapValues(_.map(_._2).sum)

          // calculate cost of added flow for each named edge
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
        }



        @tailrec
        def _solve (ind: AltIndices = startIndices, best: (Double, GenSeq[(Tag, Path)]) = (evaluate(alternatesAt(startIndices)), alternatesAt(startIndices))) : GenSeq[(Tag, Path)] = {
          advance(ind) match {
            case None => best._2
            case Some(nextInd) =>
              val nextAlts = alternatesAt(ind)
              val nextCost: Double = evaluate(nextAlts)
              val nextTuple = (nextCost, nextAlts)
              if (nextCost < best._1) {
                _solve(nextInd, nextTuple)
              } else {
                _solve(nextInd, best)
              }
          }
        }
        _solve()
      }
      val result: GenMap[LocalODPair, Path] =
        solve().map {
          tup => (unTag(tup._1.personId), tup._2)
        }.toMap

      Some(result)
    }
  }
}
