package cse.fitzgero.sorouting.algorithm.local.mssp

import cse.fitzgero.sorouting.SORoutingAsyncUnitTestTemplate
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalODPair}

class MSSPLocalDijkstrasServiceTests extends SORoutingAsyncUnitTestTemplate {
  "runService" when {
    "called with a road network and a set of od pairs" should {
      "return a map from od pairs to their shortest paths" in {
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

              // the longest shortest path should be less than or equal to 7
              odPaths.forall(od => {
                val cost: Double = od._2.map(_.cost.get.sum).sum
                cost <= 7
              }) should be (true)

              // we should have the same number of results as requests
              // since any OD pair has a solution in this graph
              odPaths.size should equal (odPairs.size)
            case None => fail()
          }
      }
    }
  }

}
