package cse.fitzgero.sorouting.algorithm.mssp.graphx

import cse.fitzgero.sorouting.algorithm.mssp._
import cse.fitzgero.sorouting.roadnetwork.graph.RoadNetwork

abstract class GraphXMSSP [A <: GraphXODPair, B <: GraphXODPath] extends MSSP[RoadNetwork, A, B] {
  protected val Infinity: Double = Double.PositiveInfinity
  protected val Zero: Double = 0.0D
  override def shortestPaths (graph: RoadNetwork, odPairs: Seq[A]): Seq[B]
}