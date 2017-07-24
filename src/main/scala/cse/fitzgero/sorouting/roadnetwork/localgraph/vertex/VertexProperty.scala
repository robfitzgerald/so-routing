package cse.fitzgero.sorouting.roadnetwork.localgraph.vertex

abstract class VertexPosition extends Serializable {}

abstract class VertexProperty [P <: VertexPosition] (val position: P) extends Serializable {}
