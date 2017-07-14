package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class ActivityNodeTests extends SORoutingUnitTestTemplate {
  "ActivityNode" when {
    "activity with end_time converted to xml" should {
      "produce a correctly formed xml node" in {
        val firstNode = MorningActivity("home", 0D, 1D, 1L, "2", EndTime(LocalTime.parse("09:00:00"))).toXml.attributes.asAttrMap
        firstNode("type") should equal ("home")
        firstNode("x") should equal ("0.0")
        firstNode("y") should equal ("1.0")
        firstNode("link") should equal ("2")
        firstNode("end_time") should equal ("09:00:00")
        firstNode.size should be (5)
      }
    }
    "activity with dur converted to xml" should {
      "produce a correctly formed xml node" in {
        val secondNode = MiddayActivity("work", 123D, 456D, 13L, "101", EndTime(LocalTime.parse("00:00:10"))).toXml.attributes.asAttrMap
        secondNode("type") should equal ("work")
        secondNode("x") should equal ("123.0")
        secondNode("y") should equal ("456.0")
        secondNode("link") should equal ("101")
        secondNode("end_time") should equal ("00:00:10")
        secondNode.size should be (5)
      }
    }
    "activity with no options converted to xml" should {
      "produce a correctly formed xml node" in {
        val thirdNode = EveningActivity("home", 0D, 1D, 1L, "2").toXml.attributes.asAttrMap
        thirdNode("type") should equal ("home")
        thirdNode("x") should equal ("0.0")
        thirdNode("y") should equal ("1.0")
        thirdNode("link") should equal ("2")
        thirdNode.size should be (4)
      }
    }
  }
}
