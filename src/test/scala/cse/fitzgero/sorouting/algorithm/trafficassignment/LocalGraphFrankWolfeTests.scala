package cse.fitzgero.sorouting.algorithm.trafficassignment

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph._
import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp._
import cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph._
import cse.fitzgero.sorouting.matsimrunner.population.{Population, PopulationFactory, RandomPopulationConfig}
import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, TestCostFunction}
import cse.fitzgero.sorouting.roadnetwork.localgraph.LocalGraphMATSimFactory

import scala.collection.GenSeq
import scala.collection.parallel.ParSeq
import scala.xml.XML

class LocalGraphFrankWolfeTests extends SORoutingUnitTestTemplate {
  "LocalGraphFrankWolfe" when {
    val networkFilePath: String =   "src/test/resources/LocalGraphFrankWolfeTests/network.xml"
    val snapshotFilePath: String =   "src/test/resources/LocalGraphFrankWolfeTests/snapshot.xml"
    val networkNoSolutionFilePath: String = "src/test/resources/LocalGraphFrankWolfeTests/networkNoSolution.xml"
    val equilNetwork: String =  "src/test/resources/LocalGraphFrankWolfeTests/network-matsim-example-equil.xml"
    val equilSnapshot: String = "src/test/resources/LocalGraphFrankWolfeTests/snapshot-matsim-example-equil.xml"
    val ryeNetworkFilePath: String = "src/main/resources/matsimNetworks/RyeNetwork.xml"
    "initializeFlows" when {
      "called with a road network which has some flow assignments" should {
        "reset those to zeroes" in {
          val rand = new scala.util.Random
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFile(networkFilePath).get
          val edgesWithRandomFlows = graph.edgeAttrs.map(_.copy(flowUpdate = rand.nextDouble() * 10D)).toSeq
          val graphWithRandomFlows = graph.replaceEdgeList(edgesWithRandomFlows)
          val result = LocalGraphFrankWolfe.initializeFlows(graphWithRandomFlows)
          graphWithRandomFlows.edgeAttrs.map(_.flow).sum should be > 0D
          result.edgeAttrs.map(_.flow).sum should equal (0D)
        }
      }
    }
    "loadAllOrNothing" when {
      "run on a triangle graph where no od pair has any alternate paths" should {
        "result in an expected assignment of flow" in {
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFile(networkFilePath).get
          val odPairs: Seq[LocalGraphODPair] = Seq(
            LocalGraphODPair("", 1, 3),
            LocalGraphODPair("", 2, 1)
          )
          val result = LocalGraphFrankWolfe.generateOracleGraph(graph, odPairs)
          val inspectResult = result.edgeAttrs.map(e => e.id -> e).toMap
          inspectResult("1").flow should equal (1D)
          inspectResult("2").flow should equal (2D)
          inspectResult("3").flow should equal (1D)
        }
      }
      "run on the MATSim equil network with 100 od pairs with the same o/d" should {
        "result in an expected assignment of flow" in {
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFile(equilNetwork).get
          val odPairs: Seq[LocalGraphODPair] =
            (0 until 100)
            .map(n => LocalGraphODPair("", 1, 15))
          val result = LocalGraphFrankWolfe.generateOracleGraph(graph, odPairs)
          val inspectResult = result.edgeAttrs.map(e => e.id -> e).toMap
          // correct shortest path:
          // (1) -- [1] -- [5] -- [14] -- [20] -- [21] -- [22] -> (15)
          inspectResult.foreach(println)
          inspectResult("1").flow should equal (100D)
          inspectResult("8").flow should equal (100D)
          inspectResult("17").flow should equal (100D)
          inspectResult("20").flow should equal (100D)
          inspectResult("21").flow should equal (100D)
          inspectResult("22").flow should equal (100D)
        }
      }
      "run on the MATSim equil network with 5000 od pairs in parallel with the same o/d" should {
        "run faster than sequential" in {
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFile(equilNetwork).get
          val odPairsSeq: Seq[LocalGraphODPair] =
            (0 until 5000)
              .map(n => LocalGraphODPair("", 1, 15))
          val odPairsPar: ParSeq[LocalGraphODPair] = odPairsSeq.par
          val startSeq = System.currentTimeMillis
          val resultSeq = LocalGraphFrankWolfe.generateOracleGraph(graph, odPairsSeq)
          val seqDur = System.currentTimeMillis - startSeq
          val startPar = System.currentTimeMillis
          val resultPar = LocalGraphFrankWolfe.generateOracleGraph(graph, odPairsPar)
          val parDur = System.currentTimeMillis - startPar
          parDur should be < seqDur
        }
      }
      "on the MATSim equil network with 100 od pairs and with snapshot flows" should {
        "find an alternate route than the solution with no snapshot flows" in {
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFileAndSnapshot(equilNetwork, equilSnapshot).get
          val odPairs: Seq[LocalGraphODPair] =
            (0 until 100)
              .map(n => LocalGraphODPair("", 1, 15))
          // has a traffic jam on link [5]
          val result = LocalGraphFrankWolfe.generateOracleGraph(graph, odPairs)
          val inspectResult = result.edgeAttrs.map(e => e.id -> e).toMap
          inspectResult.foreach(println)
          inspectResult("5").flow should equal (0D)
          inspectResult("1").flow should equal (100D)
          inspectResult("8").flow should equal (100D)
          inspectResult("17").flow should equal (100D)
          inspectResult("20").flow should equal (100D)
          inspectResult("21").flow should equal (100D)
          inspectResult("22").flow should equal (100D)
        }
      }
    }
    "calculateCurrentFlows" when {
      "3 node network" when {
        val previousGraph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFile(networkFilePath).get
        val odPairs: Seq[LocalGraphODPair] = Seq(
          LocalGraphODPair("", 1, 3),
          LocalGraphODPair("", 2, 1)
        )
        val oracleGraph = LocalGraphFrankWolfe.generateOracleGraph(previousGraph, odPairs)
        "passed a simple graph and a simple oracle and 100% phi value" should {
          "give us only the oracle values" in {
            val result = LocalGraphFrankWolfe.calculateCurrentFlows(previousGraph, oracleGraph, LocalGraphFrankWolfe.Phi(1D))
            val inspectResult = result.edgeAttrs.map(e => e.id -> e).toMap
            inspectResult("1").flow should equal (1D)
            inspectResult("2").flow should equal (2D)
            inspectResult("3").flow should equal (1D)
          }
        }
        "passed a simple graph and a simple oracle and 0% phi value" should {
          "give us only the previous graph values" in {
            val result = LocalGraphFrankWolfe.calculateCurrentFlows(previousGraph, oracleGraph, LocalGraphFrankWolfe.Phi(0D))
            val inspectResult = result.edgeAttrs.map(e => e.id -> e).toMap
            inspectResult("1").flow should equal (0D)
            inspectResult("2").flow should equal (0D)
            inspectResult("3").flow should equal (0D)
          }
        }
        "passed a simple graph and a simple oracle and 50% phi value" should {
          "give us values halfway between previous and oracle" in {
            val result = LocalGraphFrankWolfe.calculateCurrentFlows(previousGraph, oracleGraph, LocalGraphFrankWolfe.Phi(0.5D))
            val inspectResult = result.edgeAttrs.map(e => e.id -> e).toMap
            inspectResult("1").flow should equal (0.5D)
            inspectResult("2").flow should equal (1D)
            inspectResult("3").flow should equal (0.5D)
          }
        }
      }
      "equil network with snapshot" when {
        val previousGraph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFileAndSnapshot(equilNetwork, equilSnapshot).get
        val odPairs: Vector[LocalGraphODPair] = Vector(
          LocalGraphODPair("", 1, 15),
          LocalGraphODPair("", 5, 2)
        )
        val oracleGraph = LocalGraphFrankWolfe.generateOracleGraph(previousGraph, odPairs)
        "passed the equil network with snapshot and an oracle and a 100% phi value" should {
          "give us only oracle flow values" in {
            val result = LocalGraphFrankWolfe.calculateCurrentFlows(previousGraph, oracleGraph, LocalGraphFrankWolfe.Phi(1.0D))
            val inspectResult = result.edgeAttrs.map(e => e.id -> e).toMap
            inspectResult.foreach(result => {
              result._2.flow should equal(oracleGraph.edgeAttrOf(result._1).get.flow)
            })
          }
        }
      }
    }
    "solve" when {
      "called with the MATSim equil example network and 500 drivers with the same od pair" should {
        "estimate a minimal cost network flow" in {
          val rand = new scala.util.Random
//          def randomNodeId(n: Int): Long = math.min(math.max(1L, (rand.nextDouble * 15.0).toLong), 15L)
          val odPairs: GenSeq[LocalGraphODPair] = (1 to 500).map(n => LocalGraphODPair("", 1, 15)).par
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFileAndSnapshot(equilNetwork, equilSnapshot).get
          val blindAssignment = LocalGraphFrankWolfe.generateOracleGraph(graph, odPairs)

          LocalGraphFrankWolfe.solve(graph, odPairs, IterationTerminationCriteria(50)) match {
            case LocalGraphFWSolverResult(result, iter, time, relGap) =>
              val fwCost = result.edgeAttrs.map(edge => edge.linkCostFlow).sum
              val aonCost = blindAssignment.edgeAttrs.map(edge => edge.linkCostFlow).sum
              fwCost should be < aonCost
            case _ => fail()
          }
        }
      }
      "called with the MATSim equil example network and 500 drivers with random od pairs" should {
        "estimate a minimal cost network flow" in {
          val rand = new scala.util.Random
          def randomNodeId(n: Int): Long = math.min(math.max(1L, (rand.nextDouble * 15.0).toLong), 15L)
          val odPairs: GenSeq[LocalGraphODPair] = (1 to 500).map(n => LocalGraphODPair("", randomNodeId(n), randomNodeId(n))).par
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFileAndSnapshot(equilNetwork, equilSnapshot).get
          val blindAssignment = LocalGraphFrankWolfe.generateOracleGraph(graph, odPairs)

          LocalGraphFrankWolfe.solve(graph, odPairs, IterationTerminationCriteria(50)) match {
            case LocalGraphFWSolverResult(result, iter, time, relGap) =>
              val fwCost = result.edgeAttrs.map(edge => edge.linkCostFlow).sum
              val aonCost = blindAssignment.edgeAttrs.map(edge => edge.linkCostFlow).sum
              fwCost should be < aonCost
              // all 9 alternate routes should have traffic
              result.edgeAttrs.foreach(_.flow should be > 0D)
            case _ => fail()
          }
        }
      }
      "called with the MATSim equil example network and 500 drivers with random od pairs" when {
        "estimate a minimal cost network flow given a relative gap termination criteria" in {
          val rand = new scala.util.Random
          def randomNodeId(n: Int): Long = math.min(math.max(1L, (rand.nextDouble * 15.0).toLong), 15L)
          val odPairs: GenSeq[LocalGraphODPair] = (1 to 500).map(n => LocalGraphODPair("", randomNodeId(n), randomNodeId(n))).par
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFileAndSnapshot(equilNetwork, equilSnapshot).get.par
          val blindAssignment = LocalGraphFrankWolfe.generateOracleGraph(graph, odPairs)

          LocalGraphFrankWolfe.solve(graph, odPairs, RelativeGapTerminationCriteria()) match {
            case LocalGraphFWSolverResult(result, iter, time, relGap) =>
              val fwCost = result.edgeAttrs.map(edge => edge.linkCostFlow).sum
              val aonCost = blindAssignment.edgeAttrs.map(edge => edge.linkCostFlow).sum
              println(s"fwCost $fwCost aonCost $aonCost")
              fwCost should be < aonCost
            case _ => fail()
          }
        }
      }
      "called with the network of Rye, Colorado and 10 drivers" should {
        "estimate a minimal cost network flow and terminate after 10 seconds" in {
          val ryeNetworkXML = XML.loadFile(ryeNetworkFilePath)
          PopulationFactory.setSeed(1)
          val population: Population = PopulationFactory.generateSimpleRandomPopulation(ryeNetworkXML, 10)
          val odPairsMSSP = population.exportTimeGroupAsODPairs(LocalTime.parse("00:00:00"), LocalTime.parse("23:59:59"))
          val odPairs = odPairsMSSP.map(od => {LocalGraphODPair(od.personId, od.srcVertex, od.dstVertex)}).par
          val graph = LocalGraphMATSimFactory(BPRCostFunction, 10 /*minutes*/).fromFile(ryeNetworkFilePath).get.par
          val blindAssignment = LocalGraphFrankWolfe.generateOracleGraph(graph, odPairs)

          LocalGraphFrankWolfe.solve(graph, odPairs, RunningTimeTerminationCriteria(10 * 1000L)) match {
            case LocalGraphFWSolverResult(result, iter, time, relGap) =>
              println(f"completed in $iter iterations and ${time / 1000.0D}%1.2f seconds")
              val fwCost = result.edgeAttrs.map(edge => edge.linkCostFlow).sum
              val aonCost = blindAssignment.edgeAttrs.map(edge => edge.linkCostFlow).sum
              println(f"frank wolfe cost: $fwCost aonCost: $aonCost improvement: ${((aonCost.toDouble - fwCost)/aonCost.toDouble) * 100D}%1.2f%%")
              fwCost should be < aonCost
            case _ => fail()
          }
        }
      }
    }
    "relativeGap" when {
      "called with two graphs whos flows are not too different" should {
        "produce a smaller relative gap" in {
          val graph = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          val thisGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flowUpdate = edgeData._2.flow + 10))
              })
          val thatGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flowUpdate = edgeData._2.flow + 11))
              })
          val result = LocalGraphFrankWolfe.relativeGap(thisGraph, thatGraph)
          result should be < 0.5
        }
      }
      "called with two graphs whos flows that differ in size by nearly 100%" should {
        "produce a larger relative gap" in {
          val graph = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          val thisGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flowUpdate = edgeData._2.flow + 10))
              })
          val thatGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flowUpdate = edgeData._2.flow + 18))
              })
          val result = LocalGraphFrankWolfe.relativeGap(thisGraph, thatGraph)
          result should be > 0.5
        }
      }
      "called with two graphs whos flows that differ greater than 100% (upper bounds test)" should {
        "be no greater than 100%" in {
          val graph = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          val thisGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flowUpdate = edgeData._2.flow + 10))
              })
          val thatGraph =
            graph
              .edges
              .map(id => (id, graph.edgeAttrOf(id).get))
              .foldLeft(graph)((newGraph, edgeData) => {
                newGraph.updateEdge(edgeData._1, edgeData._2.copy(flowUpdate = edgeData._2.flow + 5000))
              })
          val result = LocalGraphFrankWolfe.relativeGap(thisGraph, thatGraph)
          result should equal (1.0D)
        }
      }
    }
  }
}
