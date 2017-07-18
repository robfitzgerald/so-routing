package cse.fitzgero.sorouting.roadnetwork.scalagraph.vertex

abstract class VertexPosition extends Serializable {
  def apply(attrs: Map[String, String]): VertexPosition
}

abstract class VertexProperty[P <: VertexPosition](val position: P) extends Serializable {}
