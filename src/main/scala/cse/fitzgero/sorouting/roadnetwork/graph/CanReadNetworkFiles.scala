package cse.fitzgero.sorouting.roadnetwork.graph

import org.apache.spark.graphx.Graph

import scala.util.Try

trait CanReadNetworkFiles {
  def fromFile (fileName: String): Try[Graph[_, _]]
}
