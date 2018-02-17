package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.sorouting.model.roadnetwork.costfunction.{BPRCostFunction, BasicCostFunction, CostFunction}

/**
  * a tuple class for link data which should be mixed in with a cost function type, such as BPRCostFunction
  *
  * @param flow the flow of vehicles on this link
  * @param capacity the capacity of this link in vehicles per time unit
  * @param freeFlowSpeed the speed at which vehicles can pass on this link without congestion
  * @param distance the distance to travel this link
  */
case class LocalEdgeFlowAttribute (flow: Option[Double] = None,
                                   capacity: Option[Double] = None,
                                   freeFlowSpeed: Option[Double] = None,
                                   distance: Option[Double] = None) extends LocalEdgeAttribute

object LocalEdgeFlowAttribute {
  def modifyFlow(attribute: LocalEdgeFlowAttribute, flowAdjustment: Double): LocalEdgeFlowAttribute with CostFunction = {
    val updateAmount: Option[Double] = attribute.flow match {
      case Some(flow) => Some(flow + flowAdjustment)
      case None => Some(flowAdjustment)
    }
    attribute match {
      case a: LocalEdgeFlowAttribute with BasicCostFunction =>
        new LocalEdgeFlowAttribute(updateAmount, a.capacity, a.freeFlowSpeed, a.distance) with BasicCostFunction
      case a: LocalEdgeFlowAttribute with BPRCostFunction =>
        new LocalEdgeFlowAttribute(updateAmount, a.capacity, a.freeFlowSpeed, a.distance) with BPRCostFunction
    }
  }
}