package cse.fitzgero.sorouting.model.roadnetwork.local

/**
  * a tuple class for link data which should be mixed in with a cost function type, such as BPRCostFunction
  * @param flow the flow of vehicles on this link
  * @param capacity the capacity of this link in vehicles per time unit
  * @param freeFlowSpeed the speed at which vehicles can pass on this link without congestion
  * @param distance the distance to travel this link
  */
case class LocalEdgeAttribute (flow: Option[Double] = None, capacity: Option[Double] = None, freeFlowSpeed: Option[Double] = None, distance: Option[Double] = None)
