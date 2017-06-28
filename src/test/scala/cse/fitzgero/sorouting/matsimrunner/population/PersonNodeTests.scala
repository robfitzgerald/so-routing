package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class PersonNodeTests extends SORoutingUnitTestTemplate {
  "PersonNode" when {
    "toXml called" should {
      "make a pretty xml object" in {
        val a1 = MorningActivity("home", 0D, 1D, "2", EndTime(LocalTime.parse("09:00:00")))
        val a2 = MiddayActivity("work", 123D, 456D, "101", Dur(LocalTime.parse("00:00:10")))
        val a3 = EveningActivity("home", 0D, 1D, "2")
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
  }
}
