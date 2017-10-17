package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.graph.propertygraph.PropertyEdge
import cse.fitzgero.sorouting.model.roadnetwork.costfunction._

case class LocalEdge (
  id: String,
  src: String,
  dst: String,
  attribute: LocalEdgeAttribute with CostFunction
) extends PropertyEdge {
  override type VertexId = String
  override type EdgeId = String
  override type Attr = LocalEdgeAttribute with CostFunction
}

object LocalEdge {

  val MATSimFlowRate: Double = 3600D // distance units per hour

  def apply(
    id: String,
    src: String,
    dst: String,
    flow: Option[Double] = None,
    capacity: Option[Double] = None,
    freeFlowSpeed: Option[Double] = None,
    distance: Option[Double] = None,
    costFunction: Option[CostFunctionType] = None,
    algorithmFlowRate: Double = 3600D): LocalEdge = {
    val capacityScaled: Option[Double] = capacity.map(_ * (algorithmFlowRate / MATSimFlowRate))
    costFunction match {
      case None =>
        LocalEdge(id, src, dst, new LocalEdgeAttribute(flow, capacityScaled, freeFlowSpeed, distance) with BasicCostFunction)
      case Some(cost) => cost match {
        case BasicCostFunctionType =>
          LocalEdge(id, src, dst, new LocalEdgeAttribute(flow, capacityScaled, freeFlowSpeed, distance) with BasicCostFunction)
        case BPRCostFunctionType =>
          LocalEdge(id, src, dst, new LocalEdgeAttribute(flow, capacityScaled, freeFlowSpeed, distance) with BPRCostFunction)
      }
    }
  }

  /**
    * helper function to allow flow updates without losing mixin information, that will treat unset flow values as 0.0D
    * @param edge the edge to update
    * @param additionalFlow the value to update with
    * @return
    */
  def modifyFlow(edge: LocalEdge, additionalFlow: Double): LocalEdge = {
    val currentFlow: Double = edge.attribute.flow match {
      case Some(flowValue) => flowValue
      case None => 0D
    }
    edge.attribute match {
      case a: LocalEdgeAttribute with BasicCostFunction =>
        edge.copy(attribute = new LocalEdgeAttribute(Some(currentFlow + additionalFlow), a.capacity, a.freeFlowSpeed, a.distance) with BasicCostFunction)
      case a: LocalEdgeAttribute with BPRCostFunction =>
        edge.copy(attribute = new LocalEdgeAttribute(Some(currentFlow + additionalFlow), a.capacity, a.freeFlowSpeed, a.distance) with BPRCostFunction)
    }
  }
}