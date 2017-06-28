package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class ActivityNodeTests extends SORoutingUnitTestTemplate {
  "ActivityNode" when {
    "activity with end_time converted to xml" should {
      "produce a correctly formed xml node" in {
        val firstNode = ActivityNode("home", "0", "0", "0", EndTime("09:00:00")).toXml.attributes.asAttrMap
        firstNode("type") should equal ("home")
        firstNode("x") should equal ("0")
        firstNode("y") should equal ("0")
        firstNode("link") should equal ("0")
        firstNode("end_time") should equal ("09:00:00")
        firstNode.size should be (5)
      }
    }
    "activity with dur converted to xml" should {
      "produce a correctly formed xml node" in {
        val secondNode = ActivityNode("work", "10", "10", "10", Dur("00:10")).toXml.attributes.asAttrMap
        secondNode("type") should equal ("work")
        secondNode("x") should equal ("0")
        secondNode("y") should equal ("0")
        secondNode("link") should equal ("0")
        secondNode("dur") should equal ("00:10")
        secondNode.size should be (5)
      }
    }
    "activity with no options converted to xml" should {
      "produce a correctly formed xml node" in {
        val thirdNode = ActivityNode("home" , "0", "0", "0").toXml.attributes.asAttrMap
        thirdNode("type") should equal ("home")
        thirdNode("x") should equal ("0")
        thirdNode("y") should equal ("0")
        thirdNode("link") should equal ("0")
        thirdNode.size should be (4)
      }
    }
    "when passed a malformed x value" should {
      "throw an exception" in {
        val thrown = the [IllegalArgumentException] thrownBy ActivityNode("home", "a", "0", "0")
        thrown getMessage() should contain (s"${decimalPattern.toString}")
      }
    }
    "when passed a malformed y value" should {
      "throw an exception" in {
        val thrown = the [IllegalArgumentException] thrownBy ActivityNode("home", "0", "a", "0")
        thrown getMessage() should contain (s"${decimalPattern.toString}")
      }
    }
    "when passed a malformed link value" should {
      "throw an exception" in {
        val thrown = the [IllegalArgumentException] thrownBy ActivityNode("home", "0", "0", "a")
        thrown getMessage() should contain (s"${integerPattern.toString}")
      }
    }
    "when passed a malformed end_time value" should {
      "throw an exception" in {
        val thrown = the[IllegalArgumentException] thrownBy ActivityNode("home", "0", "0", "0", EndTime("01301234"))
        thrown getMessage() should contain(s"${mmssPattern.toString}")
      }
    }
    "when passed a malformed dur value" should {
      "throw an exception" in {
        val thrown = the [IllegalArgumentException] thrownBy ActivityNode("home", "0", "0", "0", Dur("0000"))
        thrown getMessage() should contain (s"${hhmmssPattern.toString}")
      }
    }
  }
}
