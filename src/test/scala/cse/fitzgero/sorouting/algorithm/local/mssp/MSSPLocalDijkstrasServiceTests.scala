package cse.fitzgero.sorouting.algorithm.local.mssp

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingAsyncUnitTestTemplate
import cse.fitzgero.sorouting.model.population.LocalRequest
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
          LocalRequest(person.toString, LocalODPair(person.toString, o, d), LocalTime.now)
        })

        MSSPLocalDijkstrasService
          .runService(graph, odPairs) map {
            case Some(msspResult) =>
              val odPaths = msspResult.result

              // the longest shortest path should be less than or equal to 7
              odPaths.forall(od => {
                val cost: Double = od.path.map(_.cost.get.sum).sum
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
