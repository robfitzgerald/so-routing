package cse.fitzgero.sorouting.matsimrunner

import cse.fitzgero.sorouting.SORoutingUnitTests
import org.matsim.api.core.v01.Id

class NetworkStateCollectorTests extends SORoutingUnitTests {
  "NetworkStateCollector" when {
    "update called on a valid LinkEnterEvent" should {
      "contain the correctly added vehicle" in {
        val network = NetworkStateCollector()
        val v1 = Id.createVehicleId(1)
        val l1 = Id.createLinkId(1)
        val data = LinkEnterData(l1, v1)
        val updatedNetwork = network.update(data)
        updatedNetwork.networkState.getOrElse(l1, false) should equal (NonEmptyLink(Set(v1)))
      }
    }
    "update called on a valid LinkLeaveEvent" should {
      "no longer contain the driver on that link" in {
        val network = NetworkStateCollector()
        val v1 = Id.createVehicleId(1)
        val l1 = Id.createLinkId(1)
        val data1 = LinkEnterData(l1, v1)
        val data2 = LinkLeaveData(l1, v1)
        val link1ShouldBeEmpty = network.update(data1).update(data2)
        link1ShouldBeEmpty.networkState.getOrElse(l1, false) should equal (EmptyLink)
      }
    }
    "toString" should {
      "prints out all links flows as tuples on separate lines" in {
        val network = NetworkStateCollector()
        val v1 = Id.createVehicleId(1)
        val v2 = Id.createVehicleId(2)
        val v3 = Id.createVehicleId(3)
        val l1 = Id.createLinkId(1)
        val l2 = Id.createLinkId(20)
        val data1 = LinkEnterData(l1, v1)
        val data2 = LinkEnterData(l1, v2)
        val data3 = LinkEnterData(l2, v3)
        val smallNetwork = network.update(data1).update(data2).update(data3)
        smallNetwork.toString should equal ("1 2\n20 1")
      }
    }
  }
}
