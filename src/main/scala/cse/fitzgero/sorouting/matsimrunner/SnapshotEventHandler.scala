package cse.fitzgero.sorouting.matsimrunner

//import org.matsim.api.core.v01.events.LinkEnterEvent
//import org.matsim.api.core.v01.events.LinkLeaveEvent
//import org.matsim.api.core.v01.events.PersonArrivalEvent
//import org.matsim.api.core.v01.events.PersonDepartureEvent
import org.matsim.api.core.v01.events.VehicleEntersTrafficEvent
import org.matsim.api.core.v01.events.VehicleLeavesTrafficEvent
import org.matsim.api.core.v01.Id
//import org.matsim.api.core.v01.events.handler.LinkEnterEventHandler
//import org.matsim.api.core.v01.events.handler.LinkLeaveEventHandler
//import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler
//import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler
import org.matsim.api.core.v01.events.handler.VehicleEntersTrafficEventHandler
import org.matsim.api.core.v01.events.handler.VehicleLeavesTrafficEventHandler
import org.matsim.api.core.v01.network.Link
import org.matsim.api.core.v01.population.Person

class SnapshotEvent {}
case class LinkEnterEvent(linkID: Id[Link], vehicleID: Id[Person]) extends SnapshotEvent
case class LinkLeaveEvent(linkID: Id[Link], vehicleID: Id[Person]) extends SnapshotEvent

/**
  * Grabs data from the simulation which is relevant for building link flow snapshots
  * @param callback a callback function which takes
  */
class SnapshotEventHandler (callback: (SnapshotEvent) => Unit) extends VehicleEntersTrafficEventHandler with VehicleLeavesTrafficEventHandler {
  override def reset(iteration: Int): Unit = {}

  override def handleEvent(event: VehicleEntersTrafficEvent): Unit = {
    callback(LinkEnterEvent(event.getLinkId, event.getPersonId))
  }

  override def handleEvent(event: VehicleLeavesTrafficEvent): Unit = {
    callback(LinkLeaveEvent(event.getLinkId, event.getPersonId))
  }

//  override def handleEvent(event: VehicleLeavesTrafficEvent): Unit = {
//    this.timeVehicleInTraffic += event.getTime
//  }
//
//  override def handleEvent(event: VehicleEntersTrafficEvent): Unit = {
//    this.timeVehicleInTraffic -= event.getTime
//  }
}
