package cse.fitzgero.sorouting.algorithm.local.mksp

import java.time.LocalTime

import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.sorouting.SORoutingAsyncUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasConfig
import cse.fitzgero.sorouting.model.population.LocalRequest
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

class MKSPLocalDijkstrasServiceTests extends SORoutingAsyncUnitTestTemplate {
  "runService" when {
    case class Config(k: Int = 1, kspBounds: Option[KSPBounds] = None, overlapThreshold: Double = 1.0D, blockSize: Int = 8)
    "called with a road network, a set of od pairs, and some ksp config" should {
      "return a map from od pairs to their k-shortest paths" in {
        val graph = TestAssets.GraphWithAlternates

        // create batch of od pairs
        val random = scala.util.Random
        def nextV: String = (random.nextInt(10) + 1).toString
        val odPairs = (1 to 10).par.map(person => {
          val (o, d) = (nextV, nextV)
          LocalRequest(person.toString, LocalODPair(person.toString, o, d), LocalTime.now)
        })

        // k shortest paths config
        val config = Config(3)

        MKSPLocalDijkstrasService
          .runService(graph, odPairs, Some(config)) map {
            case Some(mkspResult) =>
              val odPaths = mkspResult.result

              odPaths.foreach(println)

              // each solution should run from it's respective origin to it's destination (correctness)
              odPaths.forall(kspResult => {
                val src = kspResult._1.od.src
                val dst = kspResult._1.od.dst
                kspResult
                  ._2.
                  forall(
                    _.foldLeft(src)((srcVertex, segment) => {
                      val nextEdge = graph.edgeById(segment.edgeId)
                      nextEdge.get.src should equal (srcVertex)
                      nextEdge.get.dst
                    }) == dst
                  )
              }) should be (true)

              // each successive path result's cost should grow monotonically
              odPaths.values.forall(setOfAlternates => {
                setOfAlternates.toList.sliding(2).forall(pair => {
                  if (pair.size == 1) true
                  else {
                    val p1Cost: Double = pair(0).map(_.cost.get.sum).sum
                    val p2Cost: Double = pair(1).map(_.cost.get.sum).sum
                    p1Cost <= p2Cost
                  }
                })
              }) should be (true)

              // we should have the same number of results as requests
              // since any OD pair has a solution in this graph
              odPaths.size should equal (odPairs.size)

              // no path should be longer than 7
              odPaths.values.forall(_.forall(_.map(_.cost.get.sum).sum <= 7)) should be (true)

            case None => fail()
          }
      }
    }
    "called with a bigger road network, a set of od pairs, and some ksp config" should {
      "return a map from od pairs to their k-shortest paths" in {
        val graph = TestAssets.BiggerGraph

        // create batch of od pairs
        val random = scala.util.Random
        def nextV: String = (random.nextInt(25) + 1).toString
        val odPairs = (1 to 10).par.map(person => {
          val (o, d) = (nextV, nextV)
          LocalRequest(person.toString, LocalODPair(person.toString, o, d), LocalTime.now)
        })

        // k shortest paths config
        val config = Config(3)

        MKSPLocalDijkstrasService
          .runService(graph, odPairs, Some(config)) map {
          case Some(mkspResult) =>
            val odPaths = mkspResult.result

            odPaths.foreach(println)

            // each solution should run from it's respective origin to it's destination (correctness)
            odPaths.forall(kspResult => {
              val src = kspResult._1.od.src
              val dst = kspResult._1.od.dst
              kspResult
                ._2.
                forall(
                  _.foldLeft(src)((srcVertex, segment) => {
                    val nextEdge = graph.edgeById(segment.edgeId)
                    nextEdge.get.src should equal (srcVertex)
                    nextEdge.get.dst
                  }) == dst
                )
            }) should be (true)

            // each successive path result's cost should grow monotonically
            odPaths.values.forall(setOfAlternates => {
              setOfAlternates.toList.sliding(2).forall(pair => {
                if (pair.size == 1) true
                else {
                  val p1Cost: Double = pair(0).map(_.cost.get.sum).sum
                  val p2Cost: Double = pair(1).map(_.cost.get.sum).sum
                  p1Cost <= p2Cost
                }
              })
            }) should be (true)

            // no path should be longer than 10
            odPaths.values.forall(_.forall(_.map(_.cost.get.sum).sum <= 7)) should be (true)

            // we should have the same number of results as requests
            // since any OD pair has a solution in this graph
            odPaths.size should equal (odPairs.size)

          case None => fail()
        }
      }
    }
  }

}
