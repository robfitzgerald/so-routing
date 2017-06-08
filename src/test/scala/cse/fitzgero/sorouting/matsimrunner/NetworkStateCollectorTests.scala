package cse.fitzgero.sorouting.matsimrunner

import cse.fitzgero.sorouting.SORoutingUnitTests
import org.matsim.api.core.v01.Id
import org.matsim.api.core.v01.network.Link
import org.matsim.vehicles.Vehicle

import scala.collection.immutable.Map

class NetworkStateCollectorTests extends SORoutingUnitTests {
  "NetworkStateCollector class" when {
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
    "update called on a valid LinkLeaveEvent" should {
      "no longer contain the driver on that link" in {
        val network = NetworkStateCollector()
        val v1 = Id.createVehicleId(1)
        val l1 = Id.createLinkId(1)
        val data1 = LinkEnterData(0, l1, v1)
        val data2 = LinkLeaveData(0, l1, v1)
        val link1ShouldBeEmpty = network.update(data1).update(data2)
        link1ShouldBeEmpty.networkState.getOrElse(l1, false) should equal (EmptyLink)
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
          smallNetwork.toString should equal("1 2\n20 1")
        }
      }
    }
  }
  "NetworkStateCollector object" when {
    "apply" when {
      "called with no parameters" should {
        "produce an empty map of the network state" in {
          val network = NetworkStateCollector()
          network shouldBe a [Map[Id[Link], LinkData[Id[Vehicle]]]]
        }
      }
      "called with a org.matsim.api.core.v01.network.getLinks()" ignore {
        "produce a map containing all links with flows set to zero" in {}
          // MATSim does not expose Link or Node stubs for testing, so this cannot be stubbed:
//          val links: Map[Id[Link], Link] = Map(
//            (Id.createLinkId(1), new Link)
//          )
      }
    }
  }
}
