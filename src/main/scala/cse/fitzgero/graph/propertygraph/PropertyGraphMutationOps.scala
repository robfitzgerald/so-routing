package cse.fitzgero.graph.propertygraph

trait PropertyGraphMutationOps[T <: PropertyGraphMutationOps[T]] extends PropertyGraph { self: T =>
  def updateEdge(e: EdgeId, a: Edge): T
  def updateVertex(v: VertexId, a: Vertex): T
}
