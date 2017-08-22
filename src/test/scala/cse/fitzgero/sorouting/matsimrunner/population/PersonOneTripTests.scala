package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp.SimpleMSSP_ODPair
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPairByVertex

class PersonOneTripTests extends SORoutingUnitTestTemplate {
  "PersonOneTrip" when {
    val a1 = MiddayActivity("home", 0D, 1D, 1L, "10", EndTime(LocalTime.parse("09:00:00")))
    val a2 = MiddayActivity("work", 123D, 456D, 2L, "20", EndTime(LocalTime.parse("13:00:00")))
    val personOneTrip = PersonOneTrip(SimplePersonID("1"), "car", a1, a2)
    val personOneTripCombinedID = PersonOneTrip(CombinedPersonID("1", "a"), "car", a1, a2)
    "updatePath" when {
      "called with a path for a known src/dst pair" should {
        "show the updated path info at the corresponding leg" in {
          personOneTrip.leg.isInstanceOf[UnroutedLeg] should equal (true)
          val result = personOneTrip.updatePath(List("1","4","9"))
          result.leg.isInstanceOf[RoutedLeg] should equal (true)
          (result.toXml \ "plan" \ "leg").toList.head.text should equal ("10 1 4 9 20")
        }
      }
    }
    "toXml" should {
      "construct an xml <person/> node" in {
        val result = personOneTrip.toXml
        result.attribute("id").get.text should equal ("1")
        result.size should equal (1)
        val planElements = result \ "plan" \ "_"
        planElements.size should be (3)
        planElements(0).label should equal ("activity")
        planElements(0).attribute("type").get.text should equal ("home")
        planElements(1).label should equal ("leg")
        planElements(1).attribute("mode").get.text should equal ("car")
        planElements(2).label should equal ("activity")
        planElements(2).attribute("type").get.text should equal ("work")
      }
      "correctly construct a combined id" in {
        val result = personOneTripCombinedID.toXml
        result.attribute("id").get.text should equal ("1-a")
      }
    }
    "activityInTimeGroup" should {
      "return true when the person has an activity within the time group" in {
        val result = personOneTrip.activityInTimeGroup(LocalTime.parse("08:00:00"), LocalTime.parse("14:00:00"))
        result should equal (true)
      }
      "return false when the person does not have an activity in the time group" in {
        val resultOutsideBounds = personOneTrip.activityInTimeGroup(LocalTime.parse("06:00:00"), LocalTime.parse("07:00:00"))
        val resultExclusiveUpperBound = personOneTrip.activityInTimeGroup(LocalTime.parse("13:00:00"), LocalTime.parse("14:00:00"))
        resultOutsideBounds should equal (false)
        resultExclusiveUpperBound should equal (false)
      }
    }
    "toLocalGraphODPair" should {
      "construct a valid OD Pair" in {
        val result = personOneTrip.toLocalGraphODPairByVertex
        result should equal (
          LocalGraphODPairByVertex(
            personOneTrip.id.toString,
            a1.vertex,
            a2.vertex
          )
        )
      }
    }
    "alternate constructor" should {
      "produce a Person with an unrouted leg" in {

      }
    }
  }
}
