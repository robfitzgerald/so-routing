package cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx

import cse.fitzgero.sorouting.algorithm.pathsearch._
import cse.fitzgero.sorouting.roadnetwork.graphx.graph.RoadNetwork

abstract class GraphXMSSP [A <: GraphXODPair, B <: GraphXODPath] extends MSSP[RoadNetwork, A, B] with Serializable {
  protected val Infinity: Double = Double.PositiveInfinity
  protected val Zero: Double = 0.0D
  override def shortestPaths (graph: RoadNetwork, odPairs: Seq[A]): Seq[B]
}