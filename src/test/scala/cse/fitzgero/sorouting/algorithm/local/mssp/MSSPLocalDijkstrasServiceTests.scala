package cse.fitzgero.sorouting.algorithm.local.mssp

import cse.fitzgero.sorouting.SORoutingAsyncUnitTestTemplate
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

class MSSPLocalDijkstrasServiceTests extends SORoutingAsyncUnitTestTemplate {
  "runService" when {
    "called with a road network and a set of od pairs" should {
      "return a map from od pairs to their shortest paths" in {

        // sometimes resolves with 9 instead of 10 paths

        val graph: LocalGraph = TestAssets.graph
        val random = scala.util.Random
        def nextV: String = (random.nextInt(10) + 1).toString
        val odPairs = (1 to 10).par.map(person => {
          val (o, d) = (nextV, nextV)
          LocalODPair(person.toString, o, d)
        })

        MSSPLocalDijkstrasService
          .runService(graph, odPairs) map {
            case Some(msspResult) =>
              val odPaths = msspResult.result
              odPaths.foreach(println)
              odPaths.size should equal (odPairs.size)
            case None => fail()
          }
      }
    }
  }

}
