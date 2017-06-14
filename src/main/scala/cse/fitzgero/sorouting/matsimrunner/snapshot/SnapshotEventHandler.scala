package cse.fitzgero.sorouting.matsimrunner.snapshot

import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.events.handler.{LinkEnterEventHandler, LinkLeaveEventHandler, VehicleEntersTrafficEventHandler, VehicleLeavesTrafficEventHandler}
import org.matsim.api.core.v01.events.{LinkEnterEvent, LinkLeaveEvent, VehicleEntersTrafficEvent, VehicleLeavesTrafficEvent}
import org.matsim.api.core.v01.network.Link
import org.matsim.vehicles.Vehicle

sealed abstract class SnapshotEventData {}
case class NewIteration(iteration: Int) extends SnapshotEventData
sealed trait LinkEventData extends SnapshotEventData {
  def time: Int
  def linkID: Id[Link]
  def vehicleID: Id[Vehicle]
}
object LinkEventData {
  def unapply(event: LinkEventData): Option[LinkEventData] =
    Option(event) map {e => e}
}
case class LinkEnterData(time: Int, linkID: Id[Link], vehicleID: Id[Vehicle]) extends LinkEventData
case class LinkLeaveData(time: Int, linkID: Id[Link], vehicleID: Id[Vehicle]) extends LinkEventData

/**
  * Grabs data from the simulation which is relevant for building link flow snapshots
  * @param callback a callback function which takes a SnapshotEvent case class consumed by a NetworkStateCollector.update() operation
  */
class SnapshotEventHandler (callback: (SnapshotEventData) => Unit) extends VehicleEntersTrafficEventHandler with VehicleLeavesTrafficEventHandler with LinkEnterEventHandler with LinkLeaveEventHandler {
  override def reset(iteration: Int): Unit = {
    callback(NewIteration(iteration))
  }

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
