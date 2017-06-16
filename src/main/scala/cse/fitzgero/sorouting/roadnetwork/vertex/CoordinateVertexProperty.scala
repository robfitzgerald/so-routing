package cse.fitzgero.sorouting.roadnetwork.vertex
import cse.fitzgero.sorouting.roadnetwork.graph.Path

case class Euclidian (x: Double, y: Double) extends VertexPosition

/**
  * a vertex with a 2D euclidian coordinate
  * @param position x and y position of vertex
  */
case class CoordinateVertexProperty (
  override val position: Euclidian)
  extends VertexProperty[Euclidian] (position) {}
