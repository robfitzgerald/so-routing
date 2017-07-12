package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class PersonNodeTests extends SORoutingUnitTestTemplate {
  "PersonNode" when {
    "toXml called" should {
      "construct an xml <person/> node" in {
        val a1 = MorningActivity("home", 0D, 1D, 2, EndTime(LocalTime.parse("09:00:00")))
        val a2 = List(MiddayActivity("work", 123D, 456D, 101, EndTime(LocalTime.parse("00:00:10"))))
        val a3 = EveningActivity("home", 0D, 1D, 2)
        val result = PersonNode("1", "car", a1, a2, a3).toXml
        result.attribute("id").get.text should equal ("1")
        result.size should equal (1)
        val planElements = result \ "_"
        planElements.size should be (5)
        planElements(0).label should equal ("act")
        planElements(0).attribute("type").get.text should equal ("home")
        planElements(1).label should equal ("leg")
        planElements(1).attribute("mode").get.text should equal ("car")
        planElements(2).label should equal ("act")
        planElements(2).attribute("type").get.text should equal ("work")
        planElements(3).label should equal ("leg")
        planElements(3).attribute("mode").get.text should equal ("car")
        planElements(4).label should equal ("act")
        planElements(4).attribute("type").get.text should equal ("home")
      }
    }
    "constructed with multiple activities" should {
      "construct a valid xml <person/> node" in {
        val a1 = MorningActivity("home", 0D, 1D, 2, EndTime(LocalTime.parse("09:00:00")))
        val a2 = List(
          MiddayActivity("work", 123D, 456D, 101, EndTime(LocalTime.parse("06:30:00"))),
          MiddayActivity("soccer-practice", 789D, 1011D, 123, EndTime(LocalTime.parse("01:00:00")))
        )
        val a3 = EveningActivity("home", 0D, 1D, 2)
        val result = PersonNode("1", "car", a1, a2, a3).toXml

        result.attribute("id").get.text should equal ("1")
        result.size should equal (1)
        val planElements = result \ "_"
        planElements.size should be (7)
        planElements(0).label should equal ("act")
        planElements(0).attribute("type").get.text should equal ("home")
        planElements(1).label should equal ("leg")
        planElements(1).attribute("mode").get.text should equal ("car")
        planElements(2).label should equal ("act")
        planElements(2).attribute("type").get.text should equal ("work")
        planElements(3).label should equal ("leg")
        planElements(3).attribute("mode").get.text should equal ("car")
        planElements(4).label should equal ("act")
        planElements(4).attribute("type").get.text should equal ("soccer-practice")
        planElements(5).label should equal ("leg")
        planElements(5).attribute("mode").get.text should equal ("car")
        planElements(6).label should equal ("act")
        planElements(6).attribute("type").get.text should equal ("home")
      }
    }
    "constructed with leg routes" should {
      "construct a valid xml <person/> node" in {
        val a1 = MorningActivity("home", 0D, 1D, 2, EndTime(LocalTime.parse("09:00:00")))
        val a2 = List(
          MiddayActivity("work", 123D, 456D, 101, EndTime(LocalTime.parse("06:30:00"))),
          MiddayActivity("soccer-practice", 789D, 1011D, 123, EndTime(LocalTime.parse("01:00:00")))
        )
        val a3 = EveningActivity("home", 0D, 1D, 2)
        val legs = List(LegNode("car", 1, 3, List("1", "2", "3")), LegNode("car", 3, 5, List("3", "4", "5")), LegNode("car", 5, 1, List("5", "1")))
        val result = PersonNode("1", "car", a1, a2, a3, legs).toXml

        result.attribute("id").get.text should equal ("1")
        result.size should equal (1)
        val planElements = result \ "_"
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
