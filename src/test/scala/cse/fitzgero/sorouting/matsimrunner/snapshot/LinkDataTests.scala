package cse.fitzgero.sorouting.matsimrunner.snapshot

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import org.matsim.api.core.v01.Id

class LinkDataTests extends SORoutingUnitTestTemplate {
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
      "expand the set to include a new ID" in {
        val id1 = Id.createVehicleId(1)
        val id2 = Id.createVehicleId(2)
        val link: NonEmptyLink = NonEmptyLink(Set(id1))

        link.flow should equal (1)
        link.add(id2).flow should equal (2)
      }
      "not add an ID that is already on the link" in {
        val id1 = Id.createVehicleId(1)
        val link: NonEmptyLink = NonEmptyLink(Set(id1))

        link.flow should equal (1)
        link.add(id1).flow should equal (1)
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
      "have no effect (fail silently) for an ID that isn't present" in {
        val id1 = Id.createVehicleId(1)
        val id2 = Id.createVehicleId(2)
        val id3 = Id.createVehicleId(3)
        val link: NonEmptyLink = NonEmptyLink(Set(id1, id2))

        link.flow should equal (2)
        link.remove(id3).flow should equal (2)
      }
      "result in an EmptyLink when called on a link with one vehicle" in {
        val id1 = Id.createVehicleId(1)
        val link: NonEmptyLink = NonEmptyLink(Set(id1))

        link.remove(id1) should equal (EmptyLink)
      }
    }
  }
}
