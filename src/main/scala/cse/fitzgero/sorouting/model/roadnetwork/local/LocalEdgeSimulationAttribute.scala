package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.sorouting.model.roadnetwork.costfunction.{BPRCostFunction, BasicCostFunction, CostFunction}

/**
  * a tuple class for link data which should be mixed in with a cost function type, such as BPRCostFunction
  *
  * @param drivers the ids of the drivers currently residing on this edge
  * @param capacity the capacity of this link in vehicles per time unit
  * @param freeFlowSpeed the speed at which vehicles can pass on this link without congestion
  * @param distance the distance to travel this link
  */
case class LocalEdgeSimulationAttribute (drivers: Set[String] = Set(),
                                         capacity: Option[Double] = None,
                                         freeFlowSpeed: Option[Double] = None,
                                         distance: Option[Double] = None) extends LocalEdgeAttribute {
  def flow: Option[Double] =
    if (drivers.isEmpty) None
    else Some(drivers.size.toDouble)
}

object LocalEdgeSimulationAttribute {
  def modifyDrivers(attr: LocalEdgeSimulationAttribute, driver: String): LocalEdgeSimulationAttribute with CostFunction = {
    val updateDrivers: Set[String] =
      if (attr.drivers(driver)) {
        attr.drivers - driver
      } else {
        attr.drivers + driver
      }
    attr match {
      case a: LocalEdgeSimulationAttribute with BasicCostFunction =>
        new LocalEdgeSimulationAttribute(updateDrivers, a.capacity, a.freeFlowSpeed, a.distance) with BasicCostFunction
      case a: LocalEdgeSimulationAttribute with BPRCostFunction =>
        new LocalEdgeSimulationAttribute(updateDrivers, a.capacity, a.freeFlowSpeed, a.distance) with BPRCostFunction
    }
  }
}