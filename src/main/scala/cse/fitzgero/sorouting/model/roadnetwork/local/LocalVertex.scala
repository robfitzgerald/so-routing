package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.graph.propertygraph.PropertyVertex

case class LocalVertexCoordinate (x: Double, y: Double)

case class LocalVertex (override val id: String, x: Double, y: Double) extends PropertyVertex {
  override type VertexId = String
  override type Attr = LocalVertexCoordinate
  override def attribute: LocalVertexCoordinate = LocalVertexCoordinate(x, y)
}
