package cse.fitzgero.graph.propertygraph

import cse.fitzgero.graph.basicgraph.BasicGraph

trait PropertyGraph extends BasicGraph { graph =>
  override type Edge <: PropertyEdge { type VertexId = graph.VertexId; type EdgeId = graph.EdgeId }
  override type Vertex <: PropertyVertex { type VertexId = graph.VertexId }
  def edgeById(e: EdgeId): Option[Edge]
  def vertexById(v: EdgeId): Option[Vertex]
}
