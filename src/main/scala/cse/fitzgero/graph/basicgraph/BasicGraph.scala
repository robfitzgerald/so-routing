package cse.fitzgero.graph.basicgraph

trait BasicGraph { graph =>
  type VertexId
  type EdgeId
  type Edge <: BasicEdge {
    type VertexId = graph.VertexId
    type EdgeId = graph.EdgeId
  }
  type Vertex <: BasicVertex {
    type VertexId = graph.VertexId
  }
  def outEdges(v: VertexId): Iterator[EdgeId]
}
