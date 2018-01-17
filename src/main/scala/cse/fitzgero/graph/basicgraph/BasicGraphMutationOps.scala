package cse.fitzgero.graph.basicgraph

trait BasicGraphMutationOps[T <: BasicGraphMutationOps[T]] extends BasicGraph { self: T =>
  def removeEdge(e: EdgeId): T
  def removeVertex(v: VertexId): T
}
