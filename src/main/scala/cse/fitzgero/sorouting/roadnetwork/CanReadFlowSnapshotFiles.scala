package cse.fitzgero.sorouting.roadnetwork

import scala.util.Try

import cse.fitzgero.sorouting.roadnetwork.graph.RoadNetworkWrapper

trait CanReadFlowSnapshotFiles {
  def fromFileAndSnapshot (networkFilePath: String, snapshotFilePath: String): Try[RoadNetworkWrapper[_, _, _]]
}
