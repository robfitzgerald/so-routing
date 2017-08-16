package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class LegNodeTests extends SORoutingUnitTestTemplate {
  "LegNode" when {
    "toXml" when {
      "leg with path data" should {
        "create a route node" in {
          val result = UnroutedLeg("car", 1, 9, "1", "9", List("4")).toXml
          (result \ "route").text should equal ("1 4 9")
        }
      }
      "leg with path data where the terminal elements of the path are also the src/dst links" should {
        "not repeat links" in {
          val result = UnroutedLeg("car", 1, 9, "1", "9", List("1", "4", "9")).toXml
          (result \ "route").text should equal ("1 4 9")
        }
      }
    }
  }
}
