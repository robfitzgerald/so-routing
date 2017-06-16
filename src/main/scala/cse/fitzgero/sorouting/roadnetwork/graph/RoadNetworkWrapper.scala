package cse.fitzgero.sorouting.roadnetwork.graph

import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._

abstract class RoadNetworkWrapper [G] {
  def getCostFlowValues (samplePercentage: Double): Seq[Double]
  def graph: G
}

abstract class RoadNetworkOpts {
  def shortestPath [G,I](graph: RoadNetworkWrapper[G], odPairs: Seq[(I, I)]): Seq[Path]
}