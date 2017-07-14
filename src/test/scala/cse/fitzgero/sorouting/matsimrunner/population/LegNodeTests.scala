package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class LegNodeTests extends SORoutingUnitTestTemplate {
  "LegNode" when {
    "toXml with path data" should {
      "create a route node" in {
        val result = LegNode("car", 1, 9, "1", "9", List("1", "4", "9")).toXml
        (result \ "route").text should equal ("1 4 9")
      }
    }
  }
}
