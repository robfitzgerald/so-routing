package cse.fitzgero.sorouting.matsimrunner

import org.matsim.api.core.v01.events.LinkEnterEvent
import org.matsim.api.core.v01.events.LinkLeaveEvent
import org.matsim.vehicles.Vehicle
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent
import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler
import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler
import org.matsim.api.core.v01.network.Link

class SnapshotEventData {}
case class LinkEnterData(time: Int, linkID: Id[Link], vehicleID: Id[Vehicle]) extends SnapshotEventData
case class LinkLeaveData(time: Int, linkID: Id[Link], vehicleID: Id[Vehicle]) extends SnapshotEventData

/**
  * Grabs data from the simulation which is relevant for building link flow snapshots
  * @param callback a callback function which takes a SnapshotEvent case class consumed by a NetworkStateCollector.update() operation
  */
class SnapshotEventHandler (callback: (SnapshotEventData) => Unit) extends VehicleEntersTrafficEventHandler with VehicleLeavesTrafficEventHandler with LinkEnterEventHandler with LinkLeaveEventHandler {
  override def reset(iteration: Int): Unit = {}

  override def handleEvent(event: VehicleEntersTrafficEvent): Unit = {
    callback(LinkEnterData(event.getTime.toInt, event.getLinkId, event.getVehicleId))
  }

  override def handleEvent(event: VehicleLeavesTrafficEvent): Unit = {
    callback(LinkLeaveData(event.getTime.toInt, event.getLinkId, event.getVehicleId))
  }

  override def handleEvent(event: LinkEnterEvent): Unit = {
    callback(LinkEnterData(event.getTime.toInt, event.getLinkId, event.getVehicleId))
  }

  override def handleEvent(event: LinkLeaveEvent): Unit = {
    callback(LinkLeaveData(event.getTime.toInt, event.getLinkId, event.getVehicleId))
  }
}
