package cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx

import cse.fitzgero.sorouting.algorithm.pathsearch._
import cse.fitzgero.sorouting.algorithm.pathsearch.od.graphx.{GraphXODPair, GraphXODPath}
import cse.fitzgero.sorouting.roadnetwork.graphx._

abstract class GraphXMSSP [A <: GraphXODPair, B <: GraphXODPath] extends MSSP[GraphxRoadNetwork, A, B] with Serializable {
  protected val Infinity: Double = Double.PositiveInfinity
  protected val Zero: Double = 0.0D
  override def shortestPaths (graph: GraphxRoadNetwork, odPairs: Seq[A]): Seq[B]
}