package cse.fitzgero.sorouting.algorithm.trafficassignment

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph.simplesssp._
import cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph._
import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, TestCostFunction}
import cse.fitzgero.sorouting.roadnetwork.localgraph.LocalGraphMATSimFactory

import scala.collection.parallel.ParSeq

class LocalGraphFrankWolfe2Tests extends SORoutingUnitTestTemplate {
  "LocalGraphFrankWolfe2" when {
    val networkFilePath: String =   "src/test/resources/LocalGraphFrankWolfe2Tests/network.xml"
    val snapshotFilePath: String =   "src/test/resources/LocalGraphFrankWolfe2Tests/snapshot.xml"
    val networkNoSolutionFilePath: String = "src/test/resources/LocalGraphFrankWolfe2Tests/networkNoSolution.xml"
    val equilNetwork: String =  "src/test/resources/LocalGraphFrankWolfe2Tests/network-matsim-example-equil.xml"
    val equilSnapshot: String = "src/test/resources/LocalGraphFrankWolfe2Tests/snapshot-matsim-example-equil.xml"
    "initializeFlows" when {
      "called with a road network which has some flow assignments" should {
        "reset those to zeroes" in {
          val rand = new scala.util.Random
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFile(networkFilePath).get
          val edgesWithRandomFlows = graph.edgeAttrs.map(_.copy(flow = rand.nextDouble() * 10D)).toSeq
          val graphWithRandomFlows = graph.replaceEdgeList(edgesWithRandomFlows)
          val result = LocalGraphFrankWolfe2.initializeFlows(graphWithRandomFlows)
          graphWithRandomFlows.edgeAttrs.map(_.flow).sum should be > 0D
          result.edgeAttrs.map(_.flow).sum should equal (0D)
        }
      }
    }
    "loadAllOrNothing" when {
      "run on a triangle graph where no od pair has any alternate paths" should {
        "result in an expected assignment of flow" in {
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFile(networkFilePath).get
          val odPairs: Seq[SimpleSSSP_ODPair] = Seq(
            SimpleSSSP_ODPair(1, 3),
            SimpleSSSP_ODPair(2, 1)
          )
          val result = LocalGraphFrankWolfe2.loadAllOrNothing(graph, odPairs)
          val inspectResult = result.edgeAttrs.map(e => e.id -> e).toMap
          inspectResult(1).flow should equal (1D)
          inspectResult(2).flow should equal (2D)
          inspectResult(3).flow should equal (1D)
        }
      }
      "run on the MATSim equil network with 100 od pairs with the same o/d" should {
        "result in an expected assignment of flow" in {
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFile(equilNetwork).get
          val odPairs: Seq[SimpleSSSP_ODPair] =
            (0 until 100)
            .map(n => SimpleSSSP_ODPair(1, 15))
          val result = LocalGraphFrankWolfe2.loadAllOrNothing(graph, odPairs)
          val inspectResult = result.edgeAttrs.map(e => e.id -> e).toMap
          // correct shortest path:
          // (1) -- [1] -- [5] -- [14] -- [20] -- [21] -- [22] -> (15)
          inspectResult(1).flow should equal (100D)
          inspectResult(5).flow should equal (100D)
          inspectResult(14).flow should equal (100D)
          inspectResult(20).flow should equal (100D)
          inspectResult(21).flow should equal (100D)
          inspectResult(22).flow should equal (100D)
        }
      }
      "run on the MATSim equil network with 5000 od pairs in parallel with the same o/d" should {
        "run faster than sequential" in {
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFile(equilNetwork).get
          val odPairsSeq: Seq[SimpleSSSP_ODPair] =
            (0 until 5000)
              .map(n => SimpleSSSP_ODPair(1, 15))
          val odPairsPar: ParSeq[SimpleSSSP_ODPair] = odPairsSeq.par
          val startSeq = System.currentTimeMillis
          val resultSeq = LocalGraphFrankWolfe2.loadAllOrNothing(graph, odPairsSeq)
          val seqDur = System.currentTimeMillis - startSeq
          val startPar = System.currentTimeMillis
          val resultPar = LocalGraphFrankWolfe2.loadAllOrNothing(graph, odPairsPar)
          val parDur = System.currentTimeMillis - startPar
          parDur should be < seqDur
        }
      }
      "on the MATSim equil network with 100 od pairs and with snapshot flows" should {
        "find an alternate route than the solution with no snapshot flows" in {
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFileAndSnapshot(equilNetwork, equilSnapshot).get
          val odPairs: Seq[SimpleSSSP_ODPair] =
            (0 until 100)
              .map(n => SimpleSSSP_ODPair(1, 15))
          // has a traffic jam on link [5]
          val result = LocalGraphFrankWolfe2.loadAllOrNothing(graph, odPairs)
          val inspectResult = result.edgeAttrs.map(e => e.id -> e).toMap
          inspectResult.foreach(println)
        }
      }
    }

//    "solve" ignore {
//      "called with the MATSim equil example network" should {
//        "also do something interesting" in {
//          val rand = new scala.util.Random
//          def randomNodeId(n: Int): Long = math.min(math.max(1L, (rand.nextDouble * 15.0).toLong), 15L)
//          val odPairs: Seq[SimpleSSSP_ODPair] = (1 to 50).map(n => SimpleSSSP_ODPair(randomNodeId(n), randomNodeId(n)))
//
//          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFileAndSnapshot(equilNetwork, equilSnapshot).get
//          val blindAssignment = LocalGraphFrankWolfe2.assignment(graph, odPairs)
//
//          LocalGraphFrankWolfe2.solve(graph, odPairs, IterationTerminationCriteria(10)) match {
//            case NoTrafficAssignmentSolution(iter, time) => fail()
//            case LocalGraphFWSolverResult(result, iter, time, relGap) =>
//              println(s"~~with fw~~")
////              println(s"${result.toString}")
//              println(s"${result.edgeAttrs.map(_.allFlow).mkString(" ")}")
//              println(s"${result.edgeAttrs.map(_.allFlow).sum}")
////              println(s"${result.edgeAttrs.map(_.linkCostFlow).mkString(" ")}")
//              println(s"~~without fw~~")
////              println(s"${blindAssignment.toString}")
//              println(s"${blindAssignment.edgeAttrs.map(_.allFlow).mkString(" ")}")
//              println(s"${blindAssignment.edgeAttrs.map(_.allFlow).sum}")
////              println(s"${blindAssignment.edgeAttrs.map(_.linkCostFlow).mkString(" ")}")
//              println(s"iterations $iter time $time relGap $relGap")
//          }
//        }
//      }
//    }
//    "assignment" ignore {
//      "called with a small valid graph with no flows and valid set of od pairs" should {
//        "assign flows to the obvious edges along the most direct paths" in {
//          val graph = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
//          val odPairs: Seq[SimpleSSSP_ODPair] = Seq(
//            SimpleSSSP_ODPair(1L, 3L),
//            SimpleSSSP_ODPair(1L, 3L),
//            SimpleSSSP_ODPair(2L, 1L)
//          )
//          val result = LocalGraphFrankWolfe2.assignment(graph, odPairs)
//
//          // should result with 2 on edge 1, 3 on edge 2, and 1 on edge 3
//          result.edgeAttrOf(1L).get.flow should equal (2)
//          result.edgeAttrOf(2L).get.flow should equal (3)
//          result.edgeAttrOf(3L).get.flow should equal (1)
//        }
//      }
//      "called with a small graph where no solution can be found" should {
//        "make no change" in {
//          val graph = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkNoSolutionFilePath).get
//          val odPairs: Seq[SimpleSSSP_ODPair] = Seq(
//            SimpleSSSP_ODPair(1L, 3L),
//            SimpleSSSP_ODPair(1L, 3L),
//            SimpleSSSP_ODPair(2L, 1L)
//          )
//          val result = LocalGraphFrankWolfe2.assignment(graph, odPairs)
//
//          // resulting graph should be unchanged
//          result should equal(graph)
//          result.edgeAttrOf(1L).get.flow should equal (0)
//          result.edgeAttrOf(2L).get.flow should equal (0)
//        }
//      }
//    }
//    "relativeGap" ignore {
//      "called with two graphs whos flows are not too different" should {
//        "produce a smaller relative gap" in {
//          val graph = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
//          val thisGraph =
//            graph
//              .edges
//              .map(id => (id, graph.edgeAttrOf(id).get))
//              .foldLeft(graph)((newGraph, edgeData) => {
//                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 10))
//              })
//          val thatGraph =
//            graph
//              .edges
//              .map(id => (id, graph.edgeAttrOf(id).get))
//              .foldLeft(graph)((newGraph, edgeData) => {
//                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 11))
//              })
//          val result = LocalGraphFrankWolfe2.relativeGap(thisGraph, thatGraph)
//          result should be < 0.5
//        }
//      }
//      "called with two graphs whos flows that differ in size by nearly 100%" should {
//        "produce a larger relative gap" in {
//          val graph = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
//          val thisGraph =
//            graph
//              .edges
//              .map(id => (id, graph.edgeAttrOf(id).get))
//              .foldLeft(graph)((newGraph, edgeData) => {
//                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 10))
//              })
//          val thatGraph =
//            graph
//              .edges
//              .map(id => (id, graph.edgeAttrOf(id).get))
//              .foldLeft(graph)((newGraph, edgeData) => {
//                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 18))
//              })
//          val result = LocalGraphFrankWolfe2.relativeGap(thisGraph, thatGraph)
//          result should be > 0.5
//        }
//      }
//      "called with two graphs whos flows that differ greater than 100% (upper bounds test)" should {
//        "be no greater than 100%" in {
//          val graph = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
//          val thisGraph =
//            graph
//              .edges
//              .map(id => (id, graph.edgeAttrOf(id).get))
//              .foldLeft(graph)((newGraph, edgeData) => {
//                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 10))
//              })
//          val thatGraph =
//            graph
//              .edges
//              .map(id => (id, graph.edgeAttrOf(id).get))
//              .foldLeft(graph)((newGraph, edgeData) => {
//                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flow = edgeData._2.flow + 5000))
//              })
//          val result = LocalGraphFrankWolfe2.relativeGap(thisGraph, thatGraph)
//          result should equal (1.0D)
//        }
//      }
//    }
  }
}
