//package cse.fitzgero.sorouting.algorithm.trafficassignment
//
//import cse.fitzgero.sorouting.SparkUnitTestTemplate
//import cse.fitzgero.sorouting.algorithm.flowestimation.graphx._
//import cse.fitzgero.sorouting.algorithm.shortestpath.mssp.graphx.simplemssp._
//import cse.fitzgero.sorouting.roadnetwork.costfunction._
//import cse.fitzgero.sorouting.roadnetwork.graphx.graph._
//import org.apache.spark.graphx.VertexId
//
//class GraphXFrankWolfeTests extends SparkUnitTestTemplate("FrankWolfeTests") {
//  "FrankWolfe object" when {
//    val networkFilePathSuite01: String =       "src/test/resources/FrankWolfeTests/network.xml"
//    val snapshotFilePathSuite01: String =      "src/test/resources/FrankWolfeTests/snapshot.xml"
//    //  1
//    //
//    //      2
//    //
//    //  4       6
//    //
//    //      3
//    //
//    //  5
//    val networkFilePathSuite02: String =  "src/test/resources/GraphXMacroRoadNetworkTests/network-matsim-example-equil.xml"
//    val snapshotFilePathSuite02: String = "src/test/resources/GraphXMacroRoadNetworkTests/snapshot-matsim-example-equil.xml"
//
//    val testODPairsSuite01: ODPairs = List(
//      SimpleMSSP_ODPair("1", 1L, 6L),
//      SimpleMSSP_ODPair("2", 1L, 6L),
//      SimpleMSSP_ODPair("3", 4L, 6L),
//      SimpleMSSP_ODPair("4", 4L, 6L),
//      SimpleMSSP_ODPair("5", 5L, 6L),
//      SimpleMSSP_ODPair("6", 5L, 6L)
//    )
//    val thousandODPairsSuite01: ODPairs = (1 to 1000).map(n => SimpleMSSP_ODPair(n.toString, Seq(1L, 4L, 5L)(n % 3), 6L))
//    val twoHundredODPairsSuite02: ODPairs = (1 to 200).map(n => SimpleMSSP_ODPair(n.toString, 1L, 15L))
//    "AONAssignment" ignore { // incorrect interpretation of all-or-nothing - deprecated
//      "called with a graph with an shortest but obviously congested path" should {
//        "route everything through that link anyway" in {
//          val graph = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFile(networkFilePathSuite01).get
//          val makeLinkBusy: RoadNetwork = graph.mapEdges(edge => if (edge.attr.id == "100") edge.attr.copy(flow = 200D) else edge.attr)
//
//          val (result, _) = GraphXFrankWolfe.Assignment(makeLinkBusy, testODPairsSuite01, AONFlow())
//          // all vehicles should have been routed on link "100", even though it was gnarly congested
//          result.edges.toLocalIterator.filter(_.attr.id == "100").next.attr.flow should equal (206.0D)
//        }
//      }
//    }
//    "frankWolfeFlowCalculation" when {
//      "called with equal flow values and mean (50%) phi value" should {
//        "return the same flow number" in {
//          GraphXFrankWolfe.frankWolfeFlowCalculation(GraphXFrankWolfe.Phi(0.5), 100D, 100D) should equal (100D)
//        }
//      }
//      "called with Phi Values at the extremum" should {
//        "return 100% of the corresponding flow value" in {
//          GraphXFrankWolfe.frankWolfeFlowCalculation(GraphXFrankWolfe.Phi(1.0D), 67, 100D) should equal (100D)
//          GraphXFrankWolfe.frankWolfeFlowCalculation(GraphXFrankWolfe.Phi(0.0D), 67, 100D) should equal (67D)
//        }
//      }
//    }
//    "Phi" when {
//      "called with a valid Phi value" should {
//        "produce a valid Phi object" in {
//          val phi = GraphXFrankWolfe.Phi(0.8D)
//          phi.value should equal (0.8D)
//          phi.inverse should be >= 0.19D
//          phi.inverse should be <= 0.2D
//        }
//      }
//      "called with an invalid Phi value" should {
//        "throw an exception" in {
//          val testValue: Double = 1.2D
//          val thrown = the [IllegalArgumentException] thrownBy GraphXFrankWolfe.Phi(testValue)
//          thrown.getMessage should equal (s"requirement failed: Phi is only defined for values in the range [0,1], but found ${testValue.toString}")
//        }
//      }
//    }
//    "relativeGap" when {
//      "called with two road networks" should {
//        "calculate the relative gap of those two networks" in {
//          val graph = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(networkFilePathSuite01, snapshotFilePathSuite01).get
//          val (currentIteration, _) = GraphXFrankWolfe.Assignment(graph, testODPairsSuite01, CostFlow())
//          val (aonAssignment, _) = GraphXFrankWolfe.Assignment(graph, testODPairsSuite01, AONFlow())
//          GraphXFrankWolfe.relativeGap(currentIteration, aonAssignment) should be < 1.0D
//        }
//      }
//    }
//    "SystemOptimalObjective" when {
//      "called" should {
//        "compute sum (linkFlow * currentLinkCost)" in {
//          val graphWithoutFlows = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFile(networkFilePathSuite01).get
//          val graphWithFlows = graphWithoutFlows.mapEdges(edge => edge.attr.copy(flow = 100D))
//          val objWithoutFlows = GraphXFrankWolfe.SystemOptimalObjective(graphWithoutFlows)
//          val objWithFlows = GraphXFrankWolfe.SystemOptimalObjective(graphWithFlows)
//          println(s"$objWithoutFlows $objWithFlows")
//          objWithoutFlows should be < objWithFlows
//        }
//        "be modular" ignore {
//          fail()
//        }
//        "guide our phi value" ignore {
//          fail()
//        }
//      }
//    }
//    "solve" when {
//      "called" should {
//        "prove to be interesting" in {
//          val graph = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(networkFilePathSuite01, snapshotFilePathSuite01).get
//          val (comparisonGraph, comparisonPaths) = GraphXFrankWolfe.Assignment(graph, thousandODPairsSuite01, CostFlow())
//          val result: GraphXFWSolverResult = GraphXFrankWolfe.solve(graph, thousandODPairsSuite01, RelativeGapTerminationCriteria(0.0001))
//          println(s"~~with fw~~")
//          result.paths.distinct.foreach(println(_))
//          result.finalNetwork.edges.toLocalIterator.foreach(edge => println(s"${edge.attr.id} ${edge.attr.flow} ${edge.attr.linkCostFlow}"))
//          println(s"~~without fw~~")
//          comparisonPaths.distinct.foreach(println(_))
//          comparisonGraph.edges.toLocalIterator.foreach(edge => println(s"${edge.attr.id} ${edge.attr.flow} ${edge.attr.linkCostFlow}"))
//        }
//      }
//      "called with the MATSim equil example network" should {
//        "also do something interesting" in {
//          val graph = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(networkFilePathSuite02, snapshotFilePathSuite02).get
//          val (comparisonGraph, comparisonPaths) = GraphXFrankWolfe.Assignment(graph, twoHundredODPairsSuite02, CostFlow())
//          val result: GraphXFWSolverResult = GraphXFrankWolfe.solve(graph, twoHundredODPairsSuite02, IterationTerminationCriteria(10))
//          println(s"~~with fw~~")
//          result.paths.distinct.foreach(println(_))
//          result.finalNetwork.edges.toLocalIterator.foreach(edge => println(s"${edge.attr.id} ${edge.attr.flow} ${edge.attr.linkCostFlow}"))
//          println(s"~~without fw~~")
//          comparisonPaths.distinct.foreach(println(_))
//          comparisonGraph.edges.toLocalIterator.foreach(edge => println(s"${edge.attr.id} ${edge.attr.flow} ${edge.attr.linkCostFlow}"))
//        }
//      }
//    }
//  }
//}
