package cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.shortestpath.SSSP
import cse.fitzgero.sorouting.algorithm.shortestpath.sssp.localgraph.simplesssp._
import cse.fitzgero.sorouting.roadnetwork.costfunction.{BPRCostFunction, TestCostFunction}
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._

class SimpleSSSPTests extends SORoutingUnitTestTemplate {
  "SimpleSSSP" when {
    val networkFilePath: String =       "src/test/resources/LocalGraphMATSimFactoryTests/network.xml"
    val snapshotFilePath: String =      "src/test/resources/LocalGraphMATSimFactoryTests/snapshot.xml"
    val equilNetworkFilePath: String =  "src/test/resources/LocalGraphMATSimFactoryTests/network-matsim-example-equil.xml"
    val equilSnapshotFilePath: String = "src/test/resources/LocalGraphMATSimFactoryTests/snapshot-matsim-example-equil.xml"
    val missingFilePath: String =       "src/test/resources/LocalGraphMATSimFactoryTests/blah-invalid.xml"
    "shortestPath" when {
      "run on two vertices in a 3 vertex network with one solution" should {
        "find that solution" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          val sssp = SimpleSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: SimpleSSSP_ODPath = sssp.shortestPath(graph, SimpleSSSP_ODPair(1L, 3L))
          result.path should equal (List("1", "2"))
//          println(result.toString)
        }
      }
      "run on a set of od pairs that is giving me trouble" should {
        "magically fix itself" in {
          val odPairs = Vector(
            SimpleSSSP_ODPair(14,9),
            SimpleSSSP_ODPair(4,1),
            SimpleSSSP_ODPair(8,2),
            SimpleSSSP_ODPair(13,1),
            SimpleSSSP_ODPair(13,11),
            SimpleSSSP_ODPair(1,1),
            SimpleSSSP_ODPair(11,9),
            SimpleSSSP_ODPair(4,5),
            SimpleSSSP_ODPair(7,14),
            SimpleSSSP_ODPair(11,1)
          )
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFile(equilNetworkFilePath).get
          val sssp = SimpleSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
          val result: Seq[SimpleSSSP_ODPath] =
            odPairs
            .map(od => {
              println(s"starting $od")
              val path = sssp.shortestPath(graph, od)
              println(s"done with $od")
              path })
          result.foreach(println)
        }
      }
    }
    "_backPropagate" when {
      "called with a set of nodes with calculated distance values" should {
        "find the path through the edges associated with those nodes" in {
          val sssp = new SimpleSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]
          val aSolution = Map(
            3L -> SimpleSSSP_SearchNode(π("102", 2L), 5D),
            2L -> SimpleSSSP_SearchNode(π("101", 1L), 5D),
            1L -> SimpleSSSP_SearchNode(Origin, 0D)
          )
          val goalVertex: VertexId = 3L
          val result: List[(EdgeId, Double)] = sssp._backPropagate(aSolution)(goalVertex)
          val path = result.map(_._1)
          val totalCost = result.map(_._2).sum
          path should equal (List("101", "102"))
          totalCost should equal (10D)
        }
      }
    }
    "djikstrasAlgorithm" when {
      "called with an itty bitty triangle graph and valid od pair" should {
        "produce the shortest path" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFile(networkFilePath).get
          val odPair = SimpleSSSP_ODPair(1L, 3L)
          val sssp = new SimpleSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]
          val result = sssp.djikstrasAlgorithm(graph, odPair)
          result.cost.sum should equal (2D)
          result.path should equal (List("1", "2"))
        }
      }
      "called with an itty bitty triangle graph, snapshot, and valid od pair" should {
        "produce the shortest path" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val odPair = SimpleSSSP_ODPair(1L, 3L)
          val sssp = new SimpleSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]
          val result = sssp.djikstrasAlgorithm(graph, odPair)
          result.cost.sum should be > 55D
          result.path should equal (List("1", "2"))
        }
      }
      "called with the MATSim Equil network and valid od pair" should {
        "produce the shortest path" in {
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(TestCostFunction).fromFileAndSnapshot(equilNetworkFilePath, equilSnapshotFilePath).get
          val odPair = SimpleSSSP_ODPair(1L, 15L)
          val sssp = new SimpleSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]
          val result = sssp.djikstrasAlgorithm(graph, odPair)
          result.path should equal (List("1", "5", "14", "20", "21", "22"))
          // @TODO: confirm this is running correctly. cost function evaluation?
        }
      }
    }
  }
}
