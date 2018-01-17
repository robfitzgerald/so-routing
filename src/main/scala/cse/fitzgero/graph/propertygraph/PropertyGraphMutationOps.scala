package cse.fitzgero.graph.propertygraph

import cse.fitzgero.graph.basicgraph.BasicGraphMutationOps

trait PropertyGraphMutationOps[T <: PropertyGraphMutationOps[T]] extends PropertyGraph with BasicGraphMutationOps[T] { self: T =>
  def updateEdge(e: EdgeId, a: Edge): T
  def updateVertex(v: VertexId, a: Vertex): T
}
