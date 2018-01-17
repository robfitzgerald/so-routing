package cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata

import cse.fitzgero.sorouting.model.roadnetwork.costfunction.CostFunction
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalEdgeAttribute

case class CongestionData(time: Int, vehicleCount: Int, congestionCost: Double)

/**
  * represents the current state of a link during a MATSim simulation
  * @param congestion for each moment in time that data was modified, an entry updating the link cost at that time
  * @param travelTime for each traveler that used this link, their reported travel time
  * @param vehicles a collection of vehicle ids and their start times, which should only contain vehicles that are currently traversing this link
  * @param edge the edge attribute associated with this link
  */
case class NewAnalyticLinkData(
  congestion: List[CongestionData] = List(),
  travelTime: List[NewAnalyticLinkData.TravelTime] = List(),
  vehicles: NewAnalyticLinkData.VehicleArrivalData = Map(),
  edge: LocalEdgeAttribute with CostFunction
) extends LinkData[AnalyticLinkDataUpdate] with NewAnalyticLinkDataOps {

  /**
    * adds a vehicle and it's arrival time to this link, and updates the congestion information
    * @param data the vehicle and time associated with this add
    * @return updated link
    */
  override def add(data: AnalyticLinkDataUpdate): NewAnalyticLinkData = {
    val vehicleUpdate = vehicles.updated(data.veh, data.t)
    val edgeUpdate = LocalEdgeAttribute.modifyFlow(this.edge, 1)
    val congestionUpdate = updateCongestion(edgeUpdate, data)
    val result = this.copy(vehicles = vehicleUpdate, congestion = congestionUpdate, edge = edgeUpdate)
    result
  }

  /**
    * removes a vehicle from this link and updates congestion and travel time information
    * @param data the vehicle and time associated with this remove
    * @return updated link
    */
  override def remove(data: AnalyticLinkDataUpdate): NewAnalyticLinkData = {
    val vehicleUpdate = vehicles - data.veh
    val edgeUpdate = LocalEdgeAttribute.modifyFlow(this.edge, -1)
    val congestionUpdate = updateCongestion(edgeUpdate, data)
    val travelTimeUpdate = updateTravelTime(data)
    val result = this.copy(vehicles = vehicleUpdate, congestion = congestionUpdate, travelTime = travelTimeUpdate, edge = edgeUpdate)
    result
  }

  override def flow: Int = vehicles.size
}

object NewAnalyticLinkData {
  type TravelTime = Int
  type VehicleArrivalData = Map[String, Int]
}