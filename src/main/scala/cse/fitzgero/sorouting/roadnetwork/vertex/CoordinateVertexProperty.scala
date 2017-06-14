package cse.fitzgero.sorouting.roadnetwork.vertex

case class Euclidian (x: Double, y: Double) extends VertexPosition

case class CoordinateVertexProperty (override val position: Euclidian)
  extends VertexProperty[Euclidian] (position) {}
