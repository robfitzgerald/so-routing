package cse.fitzgero.sorouting.roadnetwork.graph

import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._
import cse.fitzgero.sorouting.roadnetwork.path.Path

abstract class RoadNetworkWrapper [V <: VertexProperty[_], E <: EdgeProperty, I <: AnyVal] {
  def getCostFlowValues (linkIds: Seq[I]): Seq[Double]
  def shortestPath (o: Seq[(V, V)]): Seq[Path[_]]
}
