package cse.fitzgero.graph.propertygraph

trait PropertyGraphOps extends PropertyGraph {
  def outEdgeOperation[A](default: A, v: VertexId, getOp: (Edge) => A, combineOp: (A, A) => A): A
  def selectOutEdgeBy[A](v: VertexId, selectOp: (Edge) => (EdgeId, A), compareOp: ((EdgeId, A), (EdgeId, A)) => (EdgeId, A)): Option[Edge]
}
