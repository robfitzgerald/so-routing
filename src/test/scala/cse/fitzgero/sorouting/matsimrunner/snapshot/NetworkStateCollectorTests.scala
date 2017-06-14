package cse.fitzgero.sorouting.matsimrunner.snapshot

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import org.matsim.api.core.v01.Id

import scala.xml.Elem

class NetworkStateCollectorTests extends SORoutingUnitTestTemplate {
  "NetworkStateCollector class" when {
    val v1 = Id.createVehicleId(1)
    val v2 = Id.createVehicleId(2)
    val v3 = Id.createVehicleId(3)
    val l1 = Id.createLinkId(1)
    val l2 = Id.createLinkId(20)
    "addDriver" when {
      "called on a valid LinkEnterEvent" should {
        "contain the correctly added vehicle" in {
          val data = LinkEnterData(0, l1, v1)
          val updatedNetwork = NetworkStateCollector().addDriver(l1, v1)
          updatedNetwork.getLink(l1) should equal(NonEmptyLink(Set(v1)))
        }
      }
    }
    "removeDriver" when {
      "called on a valid LinkLeaveEvent" should {
        "no longer contain the driver on that link" in {
          val link1ShouldBeEmpty = NetworkStateCollector().addDriver(l1, v1).removeDriver(l1, v1)
          link1ShouldBeEmpty.getLink(l1) should equal(EmptyLink)
        }
      }
    }
    "update called on a valid LinkLeaveEvent" should {
      "no longer contain the driver on that link" in {
        val data1 = LinkEnterData(0, l1, v1)
        val data2 = LinkLeaveData(0, l1, v1)
        val link1ShouldBeEmpty = NetworkStateCollector().update(data1).update(data2)
        link1ShouldBeEmpty.networkState.getOrElse(l1, false) should equal (EmptyLink)
      }
    }

    "toString" when {
      "called" should {
        "print out all current links flows as tuples on separate lines" in {
          val smallNetwork = NetworkStateCollector().addDriver(l1, v1).addDriver(l1, v2).addDriver(l2, v3)
          smallNetwork.toString should equal("1 2\n20 1")
        }
      }
    }
    "toXML" when {
      "called" should {
        "print out all current links as valid XML based on the snapshot_v1.xsd schema" in {
          val smallNetwork = NetworkStateCollector().addDriver(l1, v1).addDriver(l1, v2).addDriver(l2, v3)
          val result: Elem = smallNetwork.toXML
          val dataMap: Map[String,String] = Map.empty[String, String] ++
            (for (link <- result \ "links" \ "link") yield {
              val attrs: Map[String,String] = link.attributes.asAttrMap
              (attrs("id"), attrs("flow"))
            })
          dataMap("1") should equal ("2")
          dataMap("20") should equal ("1")
        }
      }
    }
  }
  "NetworkStateCollector object" when {
    "apply" when {
      "called with no parameters" should {
        "produce an empty map of the network state" in {
          val network = NetworkStateCollector()
          network shouldBe a [NetworkStateCollector]
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
