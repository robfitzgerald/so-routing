package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.graphx

import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType

import scala.annotation.tailrec

package object simpleksp {
  type Weight = Double
  type Path = List[(EdgeIdType, Weight)]

  def similarityTest (omega: Double, takenPaths: Seq[Path]): (Path) => Boolean = {
    val takenLinks: Set[(EdgeIdType, Weight)] = takenPaths.flatten.toSet
    @tailrec
    def _similarityTest (path: Path, accumulator: Double = 0D): Double = {
      if (path.isEmpty) accumulator
      else {
        val nextAccum: Double = if (takenLinks(path.head)) accumulator + path.head._2 else accumulator
        _similarityTest(path.tail, nextAccum)
      }
    }
    (p: Path) => {
      // @TODO: safe denominator?
      val denom = p.map(_._2).sum
      (_similarityTest(p) / denom) < omega
    }
  }

  // @TODO: this file was copied from mssp.graphx.simplemssp and was being refactored for a graphx ksp solver
  // (that work was not completed)

//  /**
//    * Part of the Pregel message which, for some OD pair, captures the weight and path information for the current Pregel iteration
//    * @param weight total path cost for this OD pair, at this iteration (up to whatever intermediary vertex the message has traveled)
//    * @param path associated with the (possibly partial) weight, this is the possibly partial path traveled
//    */
//  case class SimpleKSP_PregelMsg(weight: Double = Double.PositiveInfinity, path: Path = List.empty[EdgeIdType])
//
//  /**
//    * Type of the Vertex Data of the Shortest Paths Graph
//    */
//  type SimpleMSSG_PregelVertex = Map[VertexId, SimpleMSSP_PregelMsg]
//  type ShortestPathsGraph = Graph[SimpleMSSG_PregelVertex, MacroscopicEdgeProperty]
//
//  case class SimpleMSSP_ODPair(personId: PersonIDType, srcVertex: VertexId, dstVertex: VertexId) extends GraphXODPair
//  type ODPairs = Seq[SimpleMSSP_ODPair]
//  case class SimpleMSSP_ODPath(personId: PersonIDType, srcVertex: VertexId, dstVertex: VertexId, path: Path) extends GraphXODPath
//  type ODPaths = Seq[SimpleMSSP_ODPath]

  /**
    * CostMethods are enumeration objects to identify which type of shortest path cost evaluation we are seeking
    */
//  sealed trait CostMethod
//  case class CostFlow() extends CostMethod
//  case class AONFlow() extends CostMethod
}
