package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx.simplemssp.SimpleMSSP_ODPair

class PersonNodeTests extends SORoutingUnitTestTemplate {
  "PersonNode" when {
    "unpackTrips" when {
      val a1 = MorningActivity("home", 0D, 1D, 1L, "10", EndTime(LocalTime.parse("09:00:00")))
      val a2 = List(MiddayActivity("work", 123D, 456D, 2L, "20", EndTime(LocalTime.parse("17:00:00"))))
      val a3 = EveningActivity("home", 0D, 1D, 1L, "10")
      val person = PersonNode("1", "car", a1, a2, a3)
      "called with the full day as the range" should {
        "return OD pairs for all trips" in {
          val result = person.unpackTrips(LocalTime.parse("00:00:00"), LocalTime.parse("23:59:59"))
          result(0) should equal (SimpleMSSP_ODPair("1",1L,2L))
          result(1) should equal (SimpleMSSP_ODPair("1",2L,1L))
          result.size should equal (2)
        }
      }
      "called with a time range where the numbers are at the extremum of the range" should {
        "return both trips" in {
          val result = person.unpackTrips(LocalTime.parse("09:00:00"), LocalTime.parse("17:00:01"))
          result(0) should equal (SimpleMSSP_ODPair("1",1L,2L))
          result(1) should equal (SimpleMSSP_ODPair("1",2L,1L))
          result.size should equal (2)
        }
      }
      "called with a range which should include one trip" should {
        "return only one trip" in {
          val result = person.unpackTrips(LocalTime.parse("08:50:00"), LocalTime.parse("09:10:00"))
          result.head should equal (SimpleMSSP_ODPair("1",1L,2L))
          result.size should equal (1)
        }
      }
      "called with trips on both bounds of the time range" should {
        "return only the lower trip, since the upper bound is exclusive" in {
          val result = person.unpackTrips(LocalTime.parse("09:00:00"), LocalTime.parse("17:00:00"))
          result(0) should equal (SimpleMSSP_ODPair("1",1L,2L))
          result.size should equal (1)
        }
      }
      "called at a time that does not include tests" should {
        "return nothing" in {
          val result = person.unpackTrips(LocalTime.parse("06:00:00"), LocalTime.parse("07:00:00"))
          result.size should equal (0)
        }
      }
      "called with two times where low is not less than high" should {
        "return nothing" in {
          val result = person.unpackTrips(LocalTime.parse("10:00:00"), LocalTime.parse("05:00:00"))
          result.size should equal (0)
        }
      }
    }
    "updatePath" when {
      "called with a path for a known src/dst pair" should {
        "show the updated path info at the corresponding leg" in {
          val a1 = MorningActivity("home", 0D, 1D, 1L, "10", EndTime(LocalTime.parse("09:00:00")))
          val a2 = List(MiddayActivity("work", 123D, 456D, 2L, "20", EndTime(LocalTime.parse("00:00:10"))))
          val a3 = EveningActivity("home", 0D, 1D, 1L, "10")
          val result = PersonNode("1", "car", a1, a2, a3).updatePath(1L, 2L, List("1","4","9"))
          (result.toXml \ "plan" \ "leg").toList.head.text should equal ("10 1 4 9 20")
        }
      }
    }
    "toXml called" should {
      "construct an xml <person/> node" in {
        val a1 = MorningActivity("home", 0D, 1D, 1L, "10", EndTime(LocalTime.parse("09:00:00")))
        val a2 = List(MiddayActivity("work", 123D, 456D, 2L, "20", EndTime(LocalTime.parse("00:00:10"))))
        val a3 = EveningActivity("home", 0D, 1D, 1L, "10")
        val result = PersonNode("1", "car", a1, a2, a3).toXml
        result.attribute("id").get.text should equal ("1")
        result.size should equal (1)
        val planElements = result \ "plan" \ "_"
        planElements.size should be (5)
        planElements(0).label should equal ("activity")
        planElements(0).attribute("type").get.text should equal ("home")
        planElements(1).label should equal ("leg")
        planElements(1).attribute("mode").get.text should equal ("car")
        planElements(2).label should equal ("activity")
        planElements(2).attribute("type").get.text should equal ("work")
        planElements(3).label should equal ("leg")
        planElements(3).attribute("mode").get.text should equal ("car")
        planElements(4).label should equal ("activity")
        planElements(4).attribute("type").get.text should equal ("home")
      }
    }
    "constructed with multiple activities" should {
      "construct a valid xml <person/> node" in {
        val a1 = MorningActivity("home", 0D, 1D, 2, "20", EndTime(LocalTime.parse("09:00:00")))
        val a2 = List(
          MiddayActivity("work", 123D, 456D, 101, "1000", EndTime(LocalTime.parse("06:30:00"))),
          MiddayActivity("soccer-practice", 789D, 1011D, 123, "2000", EndTime(LocalTime.parse("01:00:00")))
        )
        val a3 = EveningActivity("home", 0D, 1D, 2, "20")
        val result = PersonNode("1", "car", a1, a2, a3).toXml

        result.attribute("id").get.text should equal ("1")
        result.size should equal (1)
        val planElements = result \ "plan" \ "_"
        planElements.size should be (7)
        planElements(0).label should equal ("activity")
        planElements(0).attribute("type").get.text should equal ("home")
        planElements(1).label should equal ("leg")
        planElements(1).attribute("mode").get.text should equal ("car")
        planElements(2).label should equal ("activity")
        planElements(2).attribute("type").get.text should equal ("work")
        planElements(3).label should equal ("leg")
        planElements(3).attribute("mode").get.text should equal ("car")
        planElements(4).label should equal ("activity")
        planElements(4).attribute("type").get.text should equal ("soccer-practice")
        planElements(5).label should equal ("leg")
        planElements(5).attribute("mode").get.text should equal ("car")
        planElements(6).label should equal ("activity")
        planElements(6).attribute("type").get.text should equal ("home")
      }
    }
    "constructed with leg routes" should {
      "construct a valid xml <person/> node" in {
        val a1 = MorningActivity("home", 0D, 1D, 2, "20", EndTime(LocalTime.parse("09:00:00")))
        val a2 = List(
          MiddayActivity("work", 123D, 456D, 101, "1000", EndTime(LocalTime.parse("06:30:00"))),
          MiddayActivity("soccer-practice", 789D, 1011D, 123, "2000", EndTime(LocalTime.parse("01:00:00")))
        )
        val a3 = EveningActivity("home", 0D, 1D, 2, "20")
        val legs = List(UnroutedLeg("car", 1, 3, "1", "3", List("1", "2", "3")), UnroutedLeg("car", 3, 5, "3", "5", List("3", "4", "5")), UnroutedLeg("car", 5, 1, "5", "1", List("5", "1")))
        val result = PersonNode("1", "car", a1, a2, a3, legs).toXml

        result.attribute("id").get.text should equal ("1")
        result.size should equal (1)
        val planElements = result \ "plan" \ "_"
        planElements.size should be (7)

        planElements(1).label should equal ("leg")
        planElements(1).attribute("mode").get.text should equal ("car")
        planElements(1).text should equal ("1 2 3")

        planElements(3).label should equal ("leg")
        planElements(3).attribute("mode").get.text should equal ("car")
        planElements(3).text should equal ("3 4 5")

        planElements(5).label should equal ("leg")
        planElements(5).attribute("mode").get.text should equal ("car")
        planElements(5).text should equal ("5 1")

      }
    }
  }
}
