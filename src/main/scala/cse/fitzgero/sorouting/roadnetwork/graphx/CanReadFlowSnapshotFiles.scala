package cse.fitzgero.sorouting.roadnetwork.graphx

import org.apache.spark.graphx.Graph

import scala.util.Try

trait CanReadFlowSnapshotFiles {
  def fromFileAndSnapshot (networkFilePath: String, snapshotFilePath: String): Try[Graph[_, _]]
}
