package cse.fitzgero.sorouting.roadnetwork.graph

import org.apache.spark.graphx.Graph

import scala.util.Try

trait CanReadNetworkFiles[G] {
  def fromFile (fileName: String): Try[G]
}
