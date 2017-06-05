package cse.fitzgero.sorouting.matsimrunner

import org.matsim.api.core.v01.Id

import cse.fitzgero.sorouting.SORoutingUnitTests

class LinkDataTests extends SORoutingUnitTests {
  "EmptyLink" when {
    "add" should {
      "result in a NonEmptyLink" in {
        val e = EmptyLink
        val i = Id.createVehicleId(1)

        e.add(i) shouldBe a [NonEmptyLink]
      }
    }
    "remove" should {
      "throw an error" in {
        val e = EmptyLink
        val i = Id.createVehicleId(1)
        val thrown = the [java.lang.ArrayIndexOutOfBoundsException] thrownBy e.remove(i)

        thrown.getMessage should equal ("attempted to remove vehicle 1 from an EmptyLink")
      }
    }
    "flow" should {
      "be zero" in {
        EmptyLink.flow should equal (0)
      }
    }
  }
  "NonEmptyLink" when {
    "add" should {
      "expand the set to include this ID" in {
        val id1 = Id.createVehicleId(1)
        val id2 = Id.createVehicleId(2)
        val link: NonEmptyLink = NonEmptyLink(Set(id1))

        link.flow should equal (1)
        link.add(id2).flow should equal (2)
      }
    }
    "remove" should {
      "result in the set without the vehicle" in {
        val id1 = Id.createVehicleId(1)
        val id2 = Id.createVehicleId(2)
        val link: NonEmptyLink = NonEmptyLink(Set(id1, id2))

        link.flow should equal (2)
        link.remove(id1).flow should equal (1)
      }
    }
    "remove called on link with one vehicle" should {
      "result in an EmptyLink" in {
        val id1 = Id.createVehicleId(1)
        val link: NonEmptyLink = NonEmptyLink(Set(id1))

        link.remove(id1) should equal (EmptyLink)
      }
    }
  }
}
