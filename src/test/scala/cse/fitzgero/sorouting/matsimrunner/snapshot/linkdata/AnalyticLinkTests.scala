package cse.fitzgero.sorouting.matsimrunner.snapshot.linkdata

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.costfunction.TestCostFunction
import org.matsim.api.core.v01.Id

import scala.util.Random

class AnalyticLinkTests extends SORoutingUnitTestTemplate {
  "AnalyticLink" when {
    "add" should {
      "update the link" in {
        val link = AnalyticLink(TestCostFunction())
        val veh = Id.createVehicleId(1)
        val t = 100
        val data = AnalyticLinkDataUpdate(veh, t)

        // add vehicle 1 at time 100 to this link
        val result = link.add(data)

        result.hasAnalytics should be (false)
        result.vehicles(veh) should equal (t)
        result.min should equal (0)
        result.mean should equal (0)
        result.max should equal (0)
        result.stdDev should equal (0)
        result.congestion.head should equal ((t, 1, link.cost.costFlow(result.vehicles.size)))
        result.congestion.size should equal (1)
      }
    }
    "remove" should {
      "update the link" in {
        val link = AnalyticLink(TestCostFunction())
        val veh = Id.createVehicleId(1)
        val t = 100
        val data = AnalyticLinkDataUpdate(veh, t)
        val dataUpdate = AnalyticLinkDataUpdate(veh, t + 150)

        // add vehicle 1 at time 100 to this link, and remove it at time 200
        val result = link.add(data).remove(dataUpdate)

        result.hasAnalytics should be (true)
        result.vehicles.isDefinedAt(veh) should be (false)
        result.min should equal (150)
        result.mean should equal (150)
        result.max should equal (150)
        result.stdDev should equal (0)
        result.congestion.head should equal ((t, 1, link.cost.costFlow(1)))
        result.congestion.tail.head should equal ((t + 150, 0, link.cost.costFlow(0)))
        result.vehicles.size should equal (0)
        result.congestion.size should equal (2)
      }
    }
    "a number of adds and removes have occurred" should {
      "report analytics which should converge to expected value ranges" in {
        val link = AnalyticLink(TestCostFunction())
        val lowerBound = 200
        val upperBound = 4000
        val maxDuration = 200
        val lastPossibleStartTime = upperBound - maxDuration
        val count = 10000
        val rand = new scala.util.Random
        def randomStartTime(): Int = math.max(lowerBound, rand.nextInt(lastPossibleStartTime))
        def randomEndTime(startTime: Int): Int = startTime + rand.nextInt(maxDuration)

        val result = (1 to count).par.flatMap(n => {
          val s = randomStartTime()
          val e = randomEndTime(s)
          val veh = Id.createVehicleId(n)
          List(("add", AnalyticLinkDataUpdate(veh, s)), ("remove", AnalyticLinkDataUpdate(veh, e)))
        }).toIndexedSeq.sortBy(_._2.t).foldLeft(link)((linkUpdate, event) => event._1 match {
          case "add" => linkUpdate.add(event._2)
          case "remove" => linkUpdate.remove(event._2)
        })

        result.count should equal (count)
        result.stdDev should (be < 60D and be > 54D) // hey, these can be tweaked if you change (lower) count values
        result.min should be (0D)
        result.max should be (199D)
        result.mean should (be < 102D and be > 98D) // hey, these can be tweaked if you change (lower) count values
      }
    }
  }
}
