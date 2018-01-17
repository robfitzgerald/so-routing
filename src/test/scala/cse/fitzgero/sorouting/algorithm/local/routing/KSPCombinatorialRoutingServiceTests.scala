package cse.fitzgero.sorouting.algorithm.local.routing

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingAsyncUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasConfig
import cse.fitzgero.sorouting.algorithm.local.mssp.MSSPLocalDijkstrasService
import cse.fitzgero.sorouting.model.population.LocalRequest
import cse.fitzgero.sorouting.model.roadnetwork.local._

class KSPCombinatorialRoutingServiceTests extends SORoutingAsyncUnitTestTemplate {
  "KSPCombinatorialRoutingService" when {
    "called with a problem" should {
      "say 'yo, i'll solve it'" in {
        val graph = TestAssets.GraphWithAlternates

        // create batch of od pairs
        val requestCount: Int = 50
        val random = scala.util.Random
        def nextV: String = (random.nextInt(10) + 1).toString
        val odPairs = (1 to 10).par.map(person => {
          val (o, d) = (nextV, nextV)
          LocalRequest(person.toString, LocalODPair(person.toString, o, d), LocalTime.now)
        })

        // k shortest paths config
        val config = KSPLocalDijkstrasConfig(4)

        KSPCombinatorialRoutingService.runService(graph, odPairs, Some(config)) map {
          case Some(serviceResult) =>
//            serviceResult.logs.foreach(println)
//            println("~~request~~")
//            odPairs.ods.foreach(println)
//            println("~~result~~")
//            serviceResult.result.result.foreach(println)

            serviceResult.logs("algorithm.mksp.local.batch.completed") should equal (50L)
            serviceResult.result.size should equal (requestCount)
          case None => fail()
        }
      }
    }
    "when compared with MSSP" should {
      "produce a path set that is equal or lower in aggregate cost" in {
        val graph = TestAssets.GraphWithAlternates

        // create batch of od pairs
        val requestCount: Int = 50
        val random = scala.util.Random
        def nextV: String = (random.nextInt(10) + 1).toString
        val odPairs = (1 to 10).par.map(person => {
          val (o, d) = (nextV, nextV)
          LocalRequest(person.toString, LocalODPair(person.toString, o, d), LocalTime.now)
        })

        // k shortest paths config
        val config = KSPLocalDijkstrasConfig(4)

        KSPCombinatorialRoutingService.runService(graph, odPairs, Some(config)) flatMap {
          case Some(combinatorial) =>
            MSSPLocalDijkstrasService.runService(graph, odPairs) map {
              case Some(sssp) =>
                combinatorial.logs.foreach(println)
                sssp.logs.foreach(println)
                combinatorial.logs("algorithm.selection.local.cost.effect") should be <= sssp.logs("algorithm.mssp.local.cost.effect")
                combinatorial.logs("algorithm.mksp.local.batch.completed") should equal (50L)
                combinatorial.result.size should equal (requestCount)
              case None => fail()
            }
          case None => fail()
        }
      }
    }
  }
}
