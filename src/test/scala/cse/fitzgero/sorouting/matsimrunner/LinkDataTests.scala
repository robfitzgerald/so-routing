package cse.fitzgero.sorouting.matsimrunner

import org.matsim.api.core.v01.Id
import org.matsim.vehicles.Vehicle

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
  }
}
