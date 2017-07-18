package cse.fitzgero.sorouting.roadnetwork.graph

import scala.util.Try

trait CanReadFlowSnapshotFiles[G] {
  def fromFileAndSnapshot (networkFilePath: String, snapshotFilePath: String): Try[G]
}
