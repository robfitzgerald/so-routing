package cse.fitzgero.sorouting.algorithm.local.routing

import cse.fitzgero.sorouting.SORoutingAsyncUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasConfig
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
        val pairs = (1 to requestCount).par.map(person => {
          val (o, d) = (nextV, nextV)
          LocalODPair(person.toString, o, d)
        })
        val odPairs = LocalODBatch(pairs)

        // k shortest paths config
        val config = KSPLocalDijkstrasConfig(2)

        KSPCombinatorialRoutingService.runService(graph, odPairs, Some(config)) map {
          case Some(serviceResult) =>
            println("~~logs~~")
            println(" (should contain information about sssp, mksp, and selection algorithm performance")
            serviceResult.logs.foreach(println)
            println("~~request~~")
            odPairs.ods.foreach(println)
            println("~~result~~")
            serviceResult.result.result.foreach(println)
            serviceResult.result.result.size should equal (requestCount)
          case None => fail()
        }
      }
    }
  }
}
