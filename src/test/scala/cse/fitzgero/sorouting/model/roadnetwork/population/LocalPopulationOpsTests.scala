package cse.fitzgero.sorouting.model.roadnetwork.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.model.population.LocalPopulationOps

class LocalPopulationOpsTests extends SORoutingUnitTestTemplate {
  "LocalPopulationOps" when {
    "generateRequests" when {
      "passed a legit configuration" should {
        "produce a legit population" in new TestAssets.TriangleWorld {
          val requestedMeanTime = LocalTime.parse("08:30:00")
          val requestedPopulation: Int = 50
          val config = LocalPopulationOps.LocalPopulationConfig(requestedPopulation, requestedMeanTime)
          val result = LocalPopulationOps.generateRequests(graph, config)
          result.foreach(_.requestTime should equal (requestedMeanTime))
          result.size should equal (requestedPopulation)
        }
      }
      "passed a config with a time range" should {
        "produce a population whos times vary in the range [mean - time range, mean + time range]" in new TestAssets.TriangleWorld {
          val requestedMeanTime = LocalTime.parse("08:30:00")
          val requestedTimeRange = LocalTime.parse("00:30:00")
          val expectedLowerBound = LocalTime.parse("08:00:00")
          val expectedUpperBound = LocalTime.parse("09:00:00")

          val requestedPopulation: Int = 50
          val config = LocalPopulationOps.LocalPopulationConfig(requestedPopulation, requestedMeanTime, Some(requestedTimeRange))
          val result = LocalPopulationOps.generateRequests(graph, config)
          result.foreach(_.requestTime should (be > expectedLowerBound and be < expectedUpperBound))
          result.size should equal (requestedPopulation)
        }
      }
    }
    "generateXML from a request" when {
      "called with a graph and a request with valid data" should {
        "result in an expected XML object" in new TestAssets.TriangleWorld {
          val result = LocalPopulationOps.generateXML(graph, validRequest)
          val startAttr = (result \ "plan" \ "activity").head.attributes.asAttrMap
          val endAttr = (result \ "plan" \ "activity").last.attributes.asAttrMap

          val legAttr = (result \ "plan" \ "leg").head.attributes.asAttrMap
          val legChildren = (result \ "plan" \ "leg").head.child

          startAttr("x") should equal (graph.vertexById(validRequest.od.src).get.x.toString)
          startAttr("y") should equal (graph.vertexById(validRequest.od.src).get.y.toString)
          startAttr("end_time") should equal (testTime)

          endAttr("x") should equal (graph.vertexById(validRequest.od.dst).get.x.toString)
          endAttr("y") should equal (graph.vertexById(validRequest.od.dst).get.y.toString)

          legChildren.isEmpty should be (true)
          //          println(result.toString)
        }
      }
    }
    "generateXML from a response" when {
      "called with a graph and a request with valid data" should {
        "result in an expected XML object, including a route leg" in new TestAssets.TriangleWorld {
          val result = LocalPopulationOps.generateXML(graph, validResponse)

          val startAttr = (result \ "plan" \ "activity").head.attributes.asAttrMap
          val endAttr = (result \ "plan" \ "activity").last.attributes.asAttrMap

          val legAttr = (result \ "plan" \ "leg").head.attributes.asAttrMap
          val legChildren = (result \ "plan" \ "leg").head.child

          startAttr("x") should equal (graph.vertexById(validRequest.od.src).get.x.toString)
          startAttr("y") should equal (graph.vertexById(validRequest.od.src).get.y.toString)
          startAttr("end_time") should equal (testTime)

          endAttr("x") should equal (graph.vertexById(validRequest.od.dst).get.x.toString)
          endAttr("y") should equal (graph.vertexById(validRequest.od.dst).get.y.toString)

          legChildren.isEmpty should be (false)
          val route = (result \ "plan" \ "leg" \ "route").head
          route.head.text.trim should equal ("102 203")
        }
      }
    }
  }
}
