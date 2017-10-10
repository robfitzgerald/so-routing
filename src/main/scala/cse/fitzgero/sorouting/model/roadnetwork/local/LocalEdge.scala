package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.graph.propertygraph.PropertyEdge

case class LocalEdge (
  id: String,
  src: String,
  dst: String,
  attribute: LocalEdgeAttribute
) extends PropertyEdge {
  override type VertexId = String
  override type EdgeId = String
  override type Attr = LocalEdgeAttribute
}

object LocalEdge {
  def apply(
    id: String,
    src: String,
    dst: String,
    fixedFlow: Option[Double] = None,
    capacity: Option[Double] = None,
    freeFlowSpeed: Option[Double] = None,
    distance: Option[Double] = None,
    t: LocalEdgeAttributeType = LocalEdgeAttributeBPR): LocalEdge =
      t match {
        case LocalEdgeAttributeBasic =>
          LocalEdge(id, src, dst, LocalEdgeAttributeBasic(fixedFlow, capacity, freeFlowSpeed, distance))
        case LocalEdgeAttributeBPR =>
          LocalEdge(id, src, dst, LocalEdgeAttributeBPR(fixedFlow, capacity, freeFlowSpeed, distance))
      }
}