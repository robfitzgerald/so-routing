package cse.fitzgero.graph.basicgraph

trait BasicEdge {
  type VertexId
  type EdgeId
  def id: EdgeId
  def src: VertexId
  def dst: VertexId
}
