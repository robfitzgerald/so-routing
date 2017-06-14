package cse.fitzgero.sorouting.roadnetwork

import scala.util.Try

import cse.fitzgero.sorouting.roadnetwork.graph.RoadNetworkWrapper

trait CanReadNetworkFiles {
  def fromFile (fileName: String): Try[RoadNetworkWrapper[_, _, _]]
}
