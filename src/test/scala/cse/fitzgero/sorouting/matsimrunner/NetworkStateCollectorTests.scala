package cse.fitzgero.sorouting.matsimrunner

import cse.fitzgero.sorouting.SORoutingUnitTests
import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.network.Link
import org.matsim.vehicles.Vehicle

import scala.collection.immutable.Map

class NetworkStateCollectorTests extends SORoutingUnitTests {
  "addDriver" when {
    "called on a valid LinkEnterEvent" should {
      "contain the correctly added vehicle" in {
        val network = NetworkStateCollector()
        val v1 = Id.createVehicleId(1)
        val l1 = Id.createLinkId(1)
        val data = LinkEnterData(0, l1, v1)
        val updatedNetwork = network.addDriver(l1, v1)
        updatedNetwork.getLink(l1) should equal(NonEmptyLink(Set(v1)))
      }
    }
  }
  "removeDriver" when {
    "called on a valid LinkLeaveEvent" should {
      "no longer contain the driver on that link" in {
        val network = NetworkStateCollector()
        val v1 = Id.createVehicleId(1)
        val l1 = Id.createLinkId(1)
        val link1ShouldBeEmpty = network.addDriver(l1, v1).removeDriver(l1, v1)
        link1ShouldBeEmpty.getLink(l1) should equal(EmptyLink)
      }
    }
  }
  "toString" when {
    "called" should {
      "print out all current links flows as tuples on separate lines" in {
        val network = NetworkStateCollector()
        val v1 = Id.createVehicleId(1)
        val v2 = Id.createVehicleId(2)
        val v3 = Id.createVehicleId(3)
        val l1 = Id.createLinkId(1)
        val l2 = Id.createLinkId(20)
        val smallNetwork = network.addDriver(l1, v1).addDriver(l1, v2).addDriver(l2, v3)
        smallNetwork.toString should equal ("1 2\n20 1")
      }
    }
  }
}
