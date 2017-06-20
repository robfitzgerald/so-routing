package cse.fitzgero.sorouting.algorithm.shortestpath

import org.apache.spark.graphx._
import cse.fitzgero.sorouting.SparkUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.costfunction._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._
import cse.fitzgero.sorouting.roadnetwork.graph._

class GraphXPregelDijkstrasTests extends SparkUnitTestTemplate("GraphXPregelDijkstrasTests") {
  val Infinity: Double = Double.PositiveInfinity
  val equilNetworkFilePath: String =  "src/test/resources/GraphXPregelDijkstrasTests/network-matsim-example-equil.xml"
  val equilSnapshotFilePath: String = "src/test/resources/GraphXPregelDijkstrasTests/snapshot-matsim-example-equil.xml"
  "GraphXPregelDijkstras" when {
    "shortestPaths" when {
      "given a pair of vertex ids and a graph with no congestion and a constant-valued cost function" should {
        "return the correct shortest path, which should be shortest by number of links" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = GraphXMacroRoadNetwork(sc, TestCostFunction).fromFile(equilNetworkFilePath).get
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,12), (3,15), (12,1), (2, 3))

          val result: Seq[(VertexId, VertexId, List[EdgeIdType])] = GraphXPregelDijkstras.shortestPaths(graph, odPairs)

          val od1 = result.filter(_._1 == 1L).head
          val od2 = result.filter(_._1 == 3L).head
          val od3 = result.filter(_._1 == 12L).head
          val od4 = result.filter(_._1 == 2L).head

          od1._2 should equal (12L)
          od1._3 should equal (List("1", "10", "19"))
          od2._2 should equal (15L)
          od2._3 should equal (List("11", "20", "21", "22"))
          od3._2 should equal (1L)
          od3._3 should equal (List("20", "21", "22", "23"))
          od4._2 should equal (3L)
          od4._3 should equal (List("2"))
        }
      }
      "given a pair of vertex ids and a graph with some congestion and a flow-based cost function" should {
        "return the correct shortest path, which should be shortest by number of links" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(equilNetworkFilePath, equilSnapshotFilePath).get
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,12), (3,15), (12,1), (2, 3))

          val result: Seq[(VertexId, VertexId, List[EdgeIdType])] = GraphXPregelDijkstras.shortestPaths(graph, odPairs)

          val od1 = result.filter(_._1 == 1L).head
          val od2 = result.filter(_._1 == 3L).head
          val od3 = result.filter(_._1 == 12L).head
          val od4 = result.filter(_._1 == 2L).head

          od1._2 should equal (12L)
          od1._3 should equal (List("1", "8", "17"))  // because i put 500 people on link 10 ;-)
          od2._2 should equal (15L)
          od2._3 should equal (List("11", "20", "21", "22"))
          od3._2 should equal (1L)
          od3._3 should equal (List("20", "21", "22", "23"))
          od4._2 should equal (3L)
          od4._3 should equal (List("2"))
        }
      }
      "given a graph where the solution is not reachable" should {
        "produce a result for the requested origin/destination pair with the empty list as it's path" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1")),
              Edge(2,3,MacroscopicEdgeProperty("2")))))
          val odPairs: Seq[(VertexId, VertexId)] = Seq((3,1))

          val paths: Seq[(VertexId, VertexId, List[EdgeIdType])] = GraphXPregelDijkstras.shortestPaths(graph, odPairs)
          val od1 = paths.head
          od1._1 should equal (3L)
          od1._2 should equal (1L)
          od1._3 should equal (List.empty[EdgeIdType])
        }
      }
      "given a graph with flow data and all-or-nothing (AON) cost evaluation selected" should {
        "produce the flow costs as if there were no flow on the network" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(equilNetworkFilePath, equilSnapshotFilePath).get
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,12), (3,15), (12,1), (2, 3))

          val result: Seq[(VertexId, VertexId, List[EdgeIdType])] = GraphXPregelDijkstras.shortestPaths(graph, odPairs, AONFlow())

          val od1 = result.filter(_._1 == 1L).head
          val od2 = result.filter(_._1 == 3L).head
          val od3 = result.filter(_._1 == 12L).head
          val od4 = result.filter(_._1 == 2L).head

          od1._2 should equal (12L)
          od1._3 should equal (List("1", "10", "19"))  // the 500 people on link 10 are being ignored here because AON
          od2._2 should equal (15L)
          od2._3 should equal (List("11", "20", "21", "22"))
          od3._2 should equal (1L)
          od3._3 should equal (List("20", "21", "22", "23"))
          od4._2 should equal (3L)
          od4._3 should equal (List("2"))
        }
      }
    }
    "runPregelShortestPaths" when {
      val runPregelShortestPaths = PrivateMethod[Graph[SPGraphData, MacroscopicEdgeProperty]]('runPregelShortestPaths)
      "run with a 3-intersection (K3) road network and default flow costs of 1" should {
        "return a shortest paths graph" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1")),
              Edge(2,3,MacroscopicEdgeProperty("2")),
              Edge(3,1,MacroscopicEdgeProperty("3")))))
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,3))

          val result = GraphXPregelDijkstras invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SPGraphData] = result.vertices.toLocalIterator.toMap

          resultLocal(1)(1).weight should equal(0.0)
          resultLocal(2)(1).weight should equal(1.0)
          resultLocal(3)(1).weight should equal(2.0)
        }
      }
      "run with a 15-intersection road network and default flow costs of 1" should {
        "return a shortest paths graph" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = GraphXMacroRoadNetwork(sc, TestCostFunction).fromFile(equilNetworkFilePath).get
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,12), (3,15), (6,1))

          val result = GraphXPregelDijkstras invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SPGraphData] = result.vertices.toLocalIterator.toMap

          val correctSolution: Map[VertexId, Map[VertexId, WeightAndPath]] =
            Map((5,Map(1L -> WeightAndPath(2.0), 3L -> WeightAndPath(7.0), 6L -> WeightAndPath(7.0))),
            (10,Map(1L -> WeightAndPath(2.0), 3L -> WeightAndPath(7.0), 6L -> WeightAndPath(7.0))),
            (14,Map(1L -> WeightAndPath(5.0), 3L -> WeightAndPath(3.0), 6L -> WeightAndPath(3.0))),
            (1,Map(1L -> WeightAndPath(0.0), 3L -> WeightAndPath(5.0), 6L -> WeightAndPath(5.0))),
            (6,Map(1L -> WeightAndPath(2.0), 3L -> WeightAndPath(7.0), 6L -> WeightAndPath(0.0))),
            (9,Map(1L -> WeightAndPath(2.0), 3L -> WeightAndPath(7.0), 6L -> WeightAndPath(7.0))),
            (13,Map(1L -> WeightAndPath(4.0), 3L -> WeightAndPath(2.0), 6L -> WeightAndPath(2.0))),
            (2,Map(1L -> WeightAndPath(1.0), 3L -> WeightAndPath(6.0), 6L -> WeightAndPath(6.0))),
            (12,Map(1L -> WeightAndPath(3.0), 3L -> WeightAndPath(1.0), 6L -> WeightAndPath(1.0))),
            (7,Map(1L -> WeightAndPath(2.0), 3L -> WeightAndPath(7.0), 6L -> WeightAndPath(7.0))),
            (3,Map(1L -> WeightAndPath(2.0), 3L -> WeightAndPath(0.0), 6L -> WeightAndPath(7.0))),
            (11,Map(1L -> WeightAndPath(2.0), 3L -> WeightAndPath(7.0), 6L -> WeightAndPath(7.0))),
            (8,Map(1L -> WeightAndPath(2.0), 3L -> WeightAndPath(7.0), 6L -> WeightAndPath(7.0))),
            (4,Map(1L -> WeightAndPath(2.0), 3L -> WeightAndPath(7.0), 6L -> WeightAndPath(7.0))),
            (15,Map(1L -> WeightAndPath(6.0), 3L -> WeightAndPath(4.0), 6L -> WeightAndPath(4.0)))
          )

          resultLocal.foreach(intersection => {
            intersection._2.foreach(tuple => {
              val targetValue: Double = correctSolution(intersection._1)(tuple._1).weight
              tuple._2.weight should equal (targetValue)
            })
          })

          // a few example shortest paths:
          resultLocal(5L)(1L).path should equal (List("1", "4"))
          resultLocal(7L)(3L).path should equal (List("11", "20", "21", "22", "23", "1", "6"))
        }
      }
      "run with a 15-intersection road network with real flow values and a real cost function" ignore {
        "return a shortest paths graph" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(equilNetworkFilePath, equilSnapshotFilePath).get
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,12), (3,15), (6,1))

          val result = GraphXPregelDijkstras invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SPGraphData] = result.vertices.toLocalIterator.toMap

          // TODO: test something here

          resultLocal.foreach(v=> println(s"${v.toString}"))
        }
      }
      "run with a road network where a solution is not reachable for all intersections" should {
        "terminate with infinity values on non-source nodes" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1")),
              Edge(2,3,MacroscopicEdgeProperty("2")))))
          val odPairs: Seq[(VertexId, VertexId)] = Seq((3,1))

          val result = GraphXPregelDijkstras invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SPGraphData] = result.vertices.toLocalIterator.toMap

          resultLocal(1)(3).weight should equal(Infinity)
          resultLocal(2)(3).weight should equal(Infinity)
          resultLocal(3)(3).weight should equal(0.0)
        }
      }
      "run without any origin-destination pairs" should {
        "terminate with default (infinity) values" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1")),
              Edge(2,3,MacroscopicEdgeProperty("2")))))
          val odPairs: Seq[(VertexId, VertexId)] = Seq.empty[(VertexId, VertexId)]

          val result = GraphXPregelDijkstras invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SPGraphData] = result.vertices.toLocalIterator.toMap

          resultLocal(1)(3).weight should equal(Infinity)
          resultLocal(2)(3).weight should equal(Infinity)
          resultLocal(3)(3).weight should equal(Infinity)
        }
      }
    }
    "initialShorestPathsMessage" when {
      val initialShorestPathsMessage = PrivateMethod[Map[VertexId, WeightAndPath]]('initialShorestPathsMessage)
      "passed a set of origin-destination pairs" should {
        "return a map of those destinations to default weights (infinity)" in {
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,10), (2,20), (3,30))
          val result: Map[VertexId, WeightAndPath] = GraphXPregelDijkstras invokePrivate initialShorestPathsMessage(odPairs)

          Seq(1, 2, 3).foreach(v => {
            result(v).weight should equal (Double.PositiveInfinity)
            result(v).path should equal (List())
          })
        }
        "produces a map with a default value of Infinity" in {
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,10), (2,20), (3,30))
          val result: Map[VertexId, WeightAndPath] = GraphXPregelDijkstras invokePrivate initialShorestPathsMessage(odPairs)

          (4 to 10).foreach(v => {
            result.isDefinedAt(v) should equal (false)
            result(v).weight should equal (Infinity)
            result(v).path should equal (List())
          })
        }
      }
    }

    "initializeGraphVertexData" when {
      val initializeShortestPathsGraph = PrivateMethod[Graph[SPGraphData, MacroscopicEdgeProperty]]('initializeShortestPathsGraph)
      "passed a set of od pairs and a graph" should {
        "replace the vertices with the Id->Weight map which should be defined with Infinity values unless the Id corresponds to a source of an od pair" in {
          val graph: Graph[CoordinateVertexProperty, MacroscopicEdgeProperty] = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1",0,(x)=>1)),
              Edge(2,3,MacroscopicEdgeProperty("2",0,(x)=>1)),
              Edge(3,1,MacroscopicEdgeProperty("3",0,(x)=>1)))))
          val odPairs: Seq[(VertexId, VertexId)] = Seq((1,3), (2,1), (3,2))

          val result = GraphXPregelDijkstras invokePrivate initializeShortestPathsGraph(graph, odPairs)

          //shortest path searches from origins of our odPairs should have initial distance values of zero, otherwise infinity
          result.vertices.toLocalIterator.foreach(v => {
            (1 to 3).foreach({
              case x if x == v._1 => v._2(x).weight should equal (0)
              case y => v._2(y).weight should equal (Infinity)
            })

          })
        }
      }
    }

    "shortestPathVertexProgram" when {
      val shortestPathVertexProgram = PrivateMethod[SPGraphData]('shortestPathVertexProgram)
      "called with a message which does not provide better shortest path values than its local data" should {
        "return the original vertex data with an unmodified set of pathDistances" in {
          val sourceId: VertexId = 1L
          val thisId: VertexId = 42L // an arbitrary vertexId in the graph
          val thisVertex = Map[VertexId, WeightAndPath]((sourceId, WeightAndPath(3.14159)))
          val incomingMessage: Map[VertexId, WeightAndPath] = Map((sourceId, WeightAndPath(7.5)))

          val result: SPGraphData = GraphXPregelDijkstras invokePrivate shortestPathVertexProgram(thisId, thisVertex, incomingMessage)

          result.get(sourceId) should equal (thisVertex.get(sourceId))
        }
      }
      "called with the vertexId, local information, and summed neighbor messages" should {
        "update the vertex pathDistances with the new shortest path data" in {
          val sourceId: VertexId = 1L
          val thisId: VertexId = 42L // an arbitrary vertexId in the graph
          val thisVertex = Map[VertexId, WeightAndPath]((sourceId, WeightAndPath(7.5)))
          val incomingMessage: Map[VertexId, WeightAndPath] = Map((sourceId, WeightAndPath(3.14159)))

          val result: SPGraphData = GraphXPregelDijkstras invokePrivate shortestPathVertexProgram(thisId, thisVertex, incomingMessage)

          result.get(sourceId) should equal (incomingMessage.get(sourceId))
        }
      }
      "called with multiple source locations" should {
        "only update the shorter values" in {
          val thisId: VertexId = 42L // an arbitrary vertexId in the graph
          val thisVertex = Map[VertexId, WeightAndPath](
            (1L, WeightAndPath(5.0)),
            (2L, WeightAndPath(10.0)),
            (3L, WeightAndPath(15.0))
          )
          val incomingMessage: Map[VertexId, WeightAndPath] = Map(
            (1L, WeightAndPath(4.0)),
            (2L, WeightAndPath(8.0)),
            (3L, WeightAndPath(16.0))
          )

          val result: SPGraphData = GraphXPregelDijkstras invokePrivate shortestPathVertexProgram(thisId, thisVertex, incomingMessage)

          result.getOrElse(1L, WeightAndPath()).weight should equal (4.0)
          result.getOrElse(2L, WeightAndPath()).weight should equal (8.0)
          result.getOrElse(3L, WeightAndPath()).weight should equal (15.0)
        }
      }
    }

    "shortestPathSendMessage" when {
      val shortestPathSendMessageWrapper = PrivateMethod[(EdgeTriplet[SPGraphData, MacroscopicEdgeProperty]) => Iterator[(VertexId, SPGraphData)]]('shortestPathSendMessageWrapper)
//      val shortestPathSendMessage = PrivateMethod[Iterator[(VertexId, SPGraphData)]]('shortestPathSendMessage)
      "triplet where destination values dominate over any source values" should {
        "return an empty iterator" in {
          val srcAttr = Map[VertexId, WeightAndPath](
            (1L, WeightAndPath(5.0)),
            (2L, WeightAndPath(10.0)),
            (3L, WeightAndPath(15.0)),
            (4L, WeightAndPath(20.0)),
            (6L, WeightAndPath(30.0))
          )
          val dstAttr = Map[VertexId, WeightAndPath](
            (1L, WeightAndPath(5.0)),
            (2L, WeightAndPath(10.0)),
            (3L, WeightAndPath(15.0)),
            (4L, WeightAndPath(20.0)),
            (6L, WeightAndPath(30.0))
          )
          val graph = Graph[SPGraphData, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, srcAttr),
              (2, dstAttr))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty())))
          )

          // triplets foreach used here because of limited API access to triplets
          // there will be exactly one triplet which will contain the exactly one edge in the graph
          graph.triplets.toLocalIterator.foreach(thisEdge => {
            val setup = GraphXPregelDijkstras invokePrivate shortestPathSendMessageWrapper(CostFlow())
            val result: Iterator[(VertexId, SPGraphData)] = setup(thisEdge)

            result.isEmpty should be (true)  // because src + edgeweight will always be 1 greater than dest
          })
        }
      }
      "triplet with some differences between source and destination values" should {
        "combine best of src + edge weight and destination values" in {
          val srcAttr = Map[VertexId, WeightAndPath](
            (1L, WeightAndPath(5.0)),
            (2L, WeightAndPath(10.0)),
            (3L, WeightAndPath(15.0)),
            (4L, WeightAndPath(20.0)),
            (6L, WeightAndPath(30.0))
          )
          val dstAttr = Map[VertexId, WeightAndPath](
            (1L, WeightAndPath(4.0)),
            (2L, WeightAndPath(8.0)),
            (3L, WeightAndPath(16.0)),
            (4L, WeightAndPath(32.0)),
            (5L, WeightAndPath(64.0))
          )
          val graph = Graph[SPGraphData, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, srcAttr),
              (2, dstAttr))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty())))
          )

          // triplets foreach used here because of limited API access to triplets
          // there will be exactly one triplet which will contain the exactly one edge in the graph
          graph.triplets.toLocalIterator.foreach(thisEdge => {
            val setup = GraphXPregelDijkstras invokePrivate shortestPathSendMessageWrapper(CostFlow())
            val result: Iterator[(VertexId, SPGraphData)] = setup(thisEdge)

            val message = result.next._2
            message.getOrElse(1L, WeightAndPath()).weight should equal (4.0)  // destination attribute
            message.getOrElse(2L, WeightAndPath()).weight should equal (8.0)  // destination attribute
            message.getOrElse(3L, WeightAndPath()).weight should equal (16.0) // source attribute + edge weight
            message.getOrElse(4L, WeightAndPath()).weight should equal (21.0) // source attribute + edge weight
            message.getOrElse(5L, WeightAndPath()).weight should equal (64.0) // destination attribute
            message.getOrElse(6L, WeightAndPath()).weight should equal (31.0) // source attribute + edge weight
          })
        }
      }
    }

    "shortestPathMergeMessage" when {
      val shortestPathMergeMessage = PrivateMethod[SPGraphData]('shortestPathMergeMessage)
      "called with two messages with the same keys but some different values" should {
        "take the smaller values from either message" in {
          val msg1 = Map[VertexId, WeightAndPath](
            (1L, WeightAndPath(5.0)),
            (2L, WeightAndPath(10.0)),
            (3L, WeightAndPath(15.0)),
            (4L, WeightAndPath(20.0)),
            (5L, WeightAndPath(25.0))
          )
          val msg2 = Map[VertexId, WeightAndPath](
            (1L, WeightAndPath(4.0)),
            (2L, WeightAndPath(8.0)),
            (3L, WeightAndPath(16.0)),
            (4L, WeightAndPath(32.0)),
            (5L, WeightAndPath(64.0))
          )

          val result: SPGraphData = GraphXPregelDijkstras invokePrivate shortestPathMergeMessage(msg1, msg2)

          result.getOrElse(1L, WeightAndPath()).weight should equal (4.0)
          result.getOrElse(2L, WeightAndPath()).weight should equal (8.0)
          result.getOrElse(3L, WeightAndPath()).weight should equal (15.0)
          result.getOrElse(4L, WeightAndPath()).weight should equal (20.0)
          result.getOrElse(5L, WeightAndPath()).weight should equal (25.0)
        }
      }
      "called with two messages that have the same values" should {
        "return an equivalent message" in {
          val msg1 = Map[VertexId, WeightAndPath](
            (1L, WeightAndPath(5.0)),
            (2L, WeightAndPath(10.0)),
            (3L, WeightAndPath(15.0)),
            (4L, WeightAndPath(20.0)),
            (5L, WeightAndPath(25.0))
          )

          val result: SPGraphData = GraphXPregelDijkstras invokePrivate shortestPathMergeMessage(msg1, msg1)

          result.getOrElse(1L, WeightAndPath()).weight should equal (5.0)
          result.getOrElse(2L, WeightAndPath()).weight should equal (10.0)
          result.getOrElse(3L, WeightAndPath()).weight should equal (15.0)
          result.getOrElse(4L, WeightAndPath()).weight should equal (20.0)
          result.getOrElse(5L, WeightAndPath()).weight should equal (25.0)
        }
      }
    }
  }
}
