package cse.fitzgero.sorouting.algorithm.mssp.graphx.simplemssp

import cse.fitzgero.sorouting.SparkUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.costfunction._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.graph._
import cse.fitzgero.sorouting.roadnetwork.vertex._
import org.apache.spark.graphx._

class SimpleMSSPTests extends SparkUnitTestTemplate("GraphXShortestPathsTests") {
  val Infinity: Double = Double.PositiveInfinity
  val equilNetworkFilePath: String =  "src/test/resources/GraphXShortestPathsTests/network-matsim-example-equil.xml"
  val equilSnapshotFilePath: String = "src/test/resources/GraphXShortestPathsTests/snapshot-matsim-example-equil.xml"
  "GraphXShortestPathsTests" when {
    "shortestPaths" when {
      "given a pair of vertex ids and a graph with no congestion and a constant-valued cost function" should {
        "return the correct shortest path, which should be shortest by number of links" in {
          val graph: RoadNetwork = GraphXMacroRoadNetwork(sc, TestCostFunction).fromFile(equilNetworkFilePath).get
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("1",1,12), SimpleMSSP_ODPair("2",3,15), SimpleMSSP_ODPair("3",12,1), SimpleMSSP_ODPair("4",2, 3))
          val result: ODPaths = SimpleMSSP.shortestPaths(graph, odPairs)

          val od1 = result.filter(_.srcVertex == 1L).head
          val od2 = result.filter(_.srcVertex == 3L).head
          val od3 = result.filter(_.srcVertex == 12L).head
          val od4 = result.filter(_.srcVertex == 2L).head

          od1.dstVertex should equal (12L)
          od1.path should equal (List("1", "10", "19"))
          od2.dstVertex should equal (15L)
          od2.path should equal (List("11", "20", "21", "22"))
          od3.dstVertex should equal (1L)
          od3.path should equal (List("20", "21", "22", "23"))
          od4.dstVertex should equal (3L)
          od4.path should equal (List("2"))
        }
      }
      "given a pair of vertex ids and a graph with some congestion and a flow-based cost function" should {
        "return the correct shortest path, which should be shortest by number of links" in {
          val graph: RoadNetwork = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(equilNetworkFilePath, equilSnapshotFilePath).get
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("1",1,12), SimpleMSSP_ODPair("2",3,15), SimpleMSSP_ODPair("3",12,1), SimpleMSSP_ODPair("4",2, 3))

          val result: ODPaths = SimpleMSSP.shortestPaths(graph, odPairs)

          val od1 = result.filter(_.srcVertex == 1L).head
          val od2 = result.filter(_.srcVertex == 3L).head
          val od3 = result.filter(_.srcVertex == 12L).head
          val od4 = result.filter(_.srcVertex == 2L).head

          od1.dstVertex should equal (12L)
          od1.path should equal (List("1", "8", "17"))  // because i put 500 people on link 10 ;-)
          od2.dstVertex should equal (15L)
          od2.path should equal (List("11", "20", "21", "22"))
          od3.dstVertex should equal (1L)
          od3.path should equal (List("20", "21", "22", "23"))
          od4.dstVertex should equal (3L)
          od4.path should equal (List("2"))
        }
      }
      "given a graph where the solution is not reachable" should {
        "produce a result for the requested origin/destination pair with the empty list as it's path" in {
          val graph: RoadNetwork = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1")),
              Edge(2,3,MacroscopicEdgeProperty("2")))))
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("1234",3,1))

          val paths: ODPaths = SimpleMSSP.shortestPaths(graph, odPairs)
          val od1 = paths.head
          od1.personId should equal ("1234")
          od1.srcVertex should equal (3L)
          od1.dstVertex should equal (1L)
          od1.path should equal (List.empty[EdgeIdType])
        }
      }
      "given a graph with flow data and all-or-nothing (AON) cost evaluation selected" ignore {  // incorrect interpretation of All-or-nothing
        "produce the flow costs as if there were no flow on the network" in {
          val graph: RoadNetwork = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(equilNetworkFilePath, equilSnapshotFilePath).get
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("1",1,12), SimpleMSSP_ODPair("2",3,15), SimpleMSSP_ODPair("3",12,1), SimpleMSSP_ODPair("4",2, 3))

          SimpleMSSP.setCostMethod(AONFlow())
          val result: ODPaths = SimpleMSSP.shortestPaths(graph, odPairs)

          val od1 = result.filter(_.srcVertex == 1L).head
          val od2 = result.filter(_.srcVertex == 3L).head
          val od3 = result.filter(_.srcVertex == 12L).head
          val od4 = result.filter(_.srcVertex == 2L).head

          od1.dstVertex should equal (12L)
          od1.path should equal (List("1", "10", "19"))  // the 500 people on link 10 are being ignored here because AON
          od2.dstVertex should equal (15L)
          od2.path should equal (List("11", "20", "21", "22"))
          od3.dstVertex should equal (1L)
          od3.path should equal (List("20", "21", "22", "23"))
          od4.dstVertex should equal (3L)
          od4.path should equal (List("2"))
        }
      }
    }
    "runPregelShortestPaths" when {
      val runPregelShortestPaths = PrivateMethod[Graph[SimpleMSSG_PregelVertex, MacroscopicEdgeProperty]]('runPregelShortestPaths)
      "run with a 3-intersection (K3) road network and default flow costs of 1" should {
        "return a shortest paths graph" in {
          val graph: RoadNetwork = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1")),
              Edge(2,3,MacroscopicEdgeProperty("2")),
              Edge(3,1,MacroscopicEdgeProperty("3")))))
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("1",1,3))

          val result = SimpleMSSP invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SimpleMSSG_PregelVertex] = result.vertices.toLocalIterator.toMap

          resultLocal(1)(1).weight should equal(0.0)
          resultLocal(2)(1).weight should equal(1.0)
          resultLocal(3)(1).weight should equal(2.0)
        }
      }
      "run with a 15-intersection road network and default flow costs of 1" should {
        "return a shortest paths graph" in {
          val graph: RoadNetwork = GraphXMacroRoadNetwork(sc, TestCostFunction).fromFile(equilNetworkFilePath).get
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("1",1,12), SimpleMSSP_ODPair("11",3,15), SimpleMSSP_ODPair("2",6,1))

          val result = SimpleMSSP invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SimpleMSSG_PregelVertex] = result.vertices.toLocalIterator.toMap

          val correctSolution: Map[VertexId, Map[VertexId, SimpleMSSP_PregelMsg]] =
            Map((5,Map(1L -> SimpleMSSP_PregelMsg(2.0), 3L -> SimpleMSSP_PregelMsg(7.0), 6L -> SimpleMSSP_PregelMsg(7.0))),
            (10,Map(1L -> SimpleMSSP_PregelMsg(2.0), 3L -> SimpleMSSP_PregelMsg(7.0), 6L -> SimpleMSSP_PregelMsg(7.0))),
            (14,Map(1L -> SimpleMSSP_PregelMsg(5.0), 3L -> SimpleMSSP_PregelMsg(3.0), 6L -> SimpleMSSP_PregelMsg(3.0))),
            (1,Map(1L -> SimpleMSSP_PregelMsg(0.0), 3L -> SimpleMSSP_PregelMsg(5.0), 6L -> SimpleMSSP_PregelMsg(5.0))),
            (6,Map(1L -> SimpleMSSP_PregelMsg(2.0), 3L -> SimpleMSSP_PregelMsg(7.0), 6L -> SimpleMSSP_PregelMsg(0.0))),
            (9,Map(1L -> SimpleMSSP_PregelMsg(2.0), 3L -> SimpleMSSP_PregelMsg(7.0), 6L -> SimpleMSSP_PregelMsg(7.0))),
            (13,Map(1L -> SimpleMSSP_PregelMsg(4.0), 3L -> SimpleMSSP_PregelMsg(2.0), 6L -> SimpleMSSP_PregelMsg(2.0))),
            (2,Map(1L -> SimpleMSSP_PregelMsg(1.0), 3L -> SimpleMSSP_PregelMsg(6.0), 6L -> SimpleMSSP_PregelMsg(6.0))),
            (12,Map(1L -> SimpleMSSP_PregelMsg(3.0), 3L -> SimpleMSSP_PregelMsg(1.0), 6L -> SimpleMSSP_PregelMsg(1.0))),
            (7,Map(1L -> SimpleMSSP_PregelMsg(2.0), 3L -> SimpleMSSP_PregelMsg(7.0), 6L -> SimpleMSSP_PregelMsg(7.0))),
            (3,Map(1L -> SimpleMSSP_PregelMsg(2.0), 3L -> SimpleMSSP_PregelMsg(0.0), 6L -> SimpleMSSP_PregelMsg(7.0))),
            (11,Map(1L -> SimpleMSSP_PregelMsg(2.0), 3L -> SimpleMSSP_PregelMsg(7.0), 6L -> SimpleMSSP_PregelMsg(7.0))),
            (8,Map(1L -> SimpleMSSP_PregelMsg(2.0), 3L -> SimpleMSSP_PregelMsg(7.0), 6L -> SimpleMSSP_PregelMsg(7.0))),
            (4,Map(1L -> SimpleMSSP_PregelMsg(2.0), 3L -> SimpleMSSP_PregelMsg(7.0), 6L -> SimpleMSSP_PregelMsg(7.0))),
            (15,Map(1L -> SimpleMSSP_PregelMsg(6.0), 3L -> SimpleMSSP_PregelMsg(4.0), 6L -> SimpleMSSP_PregelMsg(4.0)))
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
          val graph: RoadNetwork = GraphXMacroRoadNetwork(sc, BPRCostFunction).fromFileAndSnapshot(equilNetworkFilePath, equilSnapshotFilePath).get
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("2",1,12), SimpleMSSP_ODPair("@4",3,15), SimpleMSSP_ODPair("45",6,1))

          val result = SimpleMSSP invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SimpleMSSG_PregelVertex] = result.vertices.toLocalIterator.toMap

          // TODO: test something here

          resultLocal.foreach(v=> println(s"${v.toString}"))
        }
      }
      "run with a road network where a solution is not reachable for all intersections" should {
        "terminate with infinity values on non-source nodes" in {
          val graph: RoadNetwork = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1")),
              Edge(2,3,MacroscopicEdgeProperty("2")))))
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("a",3,1))

          val result = SimpleMSSP invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SimpleMSSG_PregelVertex] = result.vertices.toLocalIterator.toMap

          resultLocal(1)(3).weight should equal(Infinity)
          resultLocal(2)(3).weight should equal(Infinity)
          resultLocal(3)(3).weight should equal(0.0)
        }
      }
      "run without any origin-destination pairs" should {
        "terminate with default (infinity) values" in {
          val graph: RoadNetwork = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1")),
              Edge(2,3,MacroscopicEdgeProperty("2")))))
          val odPairs: ODPairs = Seq.empty[SimpleMSSP_ODPair]

          val result = SimpleMSSP invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SimpleMSSG_PregelVertex] = result.vertices.toLocalIterator.toMap

          resultLocal(1)(3).weight should equal(Infinity)
          resultLocal(2)(3).weight should equal(Infinity)
          resultLocal(3)(3).weight should equal(Infinity)
        }
      }
    }
    "initialShorestPathsMessage" when {
      val initialShorestPathsMessage = PrivateMethod[Map[VertexId, SimpleMSSP_PregelMsg]]('initialShorestPathsMessage)
      "passed a set of origin-destination pairs" should {
        "return a map of those destinations to default weights (infinity)" in {
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("a",1,10), SimpleMSSP_ODPair("a",2,20), SimpleMSSP_ODPair("b",3,30))
          val result: Map[VertexId, SimpleMSSP_PregelMsg] = SimpleMSSP invokePrivate initialShorestPathsMessage(odPairs)

          Seq(1, 2, 3).foreach(v => {
            result(v).weight should equal (Double.PositiveInfinity)
            result(v).path should equal (List())
          })
        }
        "produces a map with a default value of Infinity" in {
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("a",1,10), SimpleMSSP_ODPair("a",2,20), SimpleMSSP_ODPair("b",3,30))
          val result: Map[VertexId, SimpleMSSP_PregelMsg] = SimpleMSSP invokePrivate initialShorestPathsMessage(odPairs)

          (4 to 10).foreach(v => {
            result.isDefinedAt(v) should equal (false)
            result(v).weight should equal (Infinity)
            result(v).path should equal (List())
          })
        }
      }
    }

    "initializeGraphVertexData" when {
      val initializeShortestPathsGraph = PrivateMethod[Graph[SimpleMSSG_PregelVertex, MacroscopicEdgeProperty]]('initializeShortestPathsGraph)
      "passed a set of od pairs and a graph" should {
        "replace the vertices with the Id->Weight map which should be defined with Infinity values unless the Id corresponds to a source of an od pair" in {
          val graph: RoadNetwork = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1")),
              Edge(2,3,MacroscopicEdgeProperty("2")),
              Edge(3,1,MacroscopicEdgeProperty("3")))))
          val odPairs: ODPairs = Seq(SimpleMSSP_ODPair("1",1,3), SimpleMSSP_ODPair("2",2,1), SimpleMSSP_ODPair("3",3,2))

          val result = SimpleMSSP invokePrivate initializeShortestPathsGraph(graph, odPairs)

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
      val shortestPathVertexProgram = PrivateMethod[SimpleMSSG_PregelVertex]('shortestPathVertexProgram)
      "called with a message which does not provide better shortest path values than its local data" should {
        "return the original vertex data with an unmodified set of pathDistances" in {
          val sourceId: VertexId = 1L
          val thisId: VertexId = 42L // an arbitrary vertexId in the graph
          val thisVertex = Map[VertexId, SimpleMSSP_PregelMsg]((sourceId, SimpleMSSP_PregelMsg(3.14159)))
          val incomingMessage: Map[VertexId, SimpleMSSP_PregelMsg] = Map((sourceId, SimpleMSSP_PregelMsg(7.5)))

          val result: SimpleMSSG_PregelVertex = SimpleMSSP invokePrivate shortestPathVertexProgram(thisId, thisVertex, incomingMessage)

          result.get(sourceId) should equal (thisVertex.get(sourceId))
        }
      }
      "called with the vertexId, local information, and summed neighbor messages" should {
        "update the vertex pathDistances with the new shortest path data" in {
          val sourceId: VertexId = 1L
          val thisId: VertexId = 42L // an arbitrary vertexId in the graph
          val thisVertex = Map[VertexId, SimpleMSSP_PregelMsg]((sourceId, SimpleMSSP_PregelMsg(7.5)))
          val incomingMessage: Map[VertexId, SimpleMSSP_PregelMsg] = Map((sourceId, SimpleMSSP_PregelMsg(3.14159)))

          val result: SimpleMSSG_PregelVertex = SimpleMSSP invokePrivate shortestPathVertexProgram(thisId, thisVertex, incomingMessage)

          result.get(sourceId) should equal (incomingMessage.get(sourceId))
        }
      }
      "called with multiple source locations" should {
        "only update the shorter values" in {
          val thisId: VertexId = 42L // an arbitrary vertexId in the graph
          val thisVertex = Map[VertexId, SimpleMSSP_PregelMsg](
            (1L, SimpleMSSP_PregelMsg(5.0)),
            (2L, SimpleMSSP_PregelMsg(10.0)),
            (3L, SimpleMSSP_PregelMsg(15.0))
          )
          val incomingMessage: Map[VertexId, SimpleMSSP_PregelMsg] = Map(
            (1L, SimpleMSSP_PregelMsg(4.0)),
            (2L, SimpleMSSP_PregelMsg(8.0)),
            (3L, SimpleMSSP_PregelMsg(16.0))
          )

          val result: SimpleMSSG_PregelVertex = SimpleMSSP invokePrivate shortestPathVertexProgram(thisId, thisVertex, incomingMessage)

          result.getOrElse(1L, SimpleMSSP_PregelMsg()).weight should equal (4.0)
          result.getOrElse(2L, SimpleMSSP_PregelMsg()).weight should equal (8.0)
          result.getOrElse(3L, SimpleMSSP_PregelMsg()).weight should equal (15.0)
        }
      }
    }

    "shortestPathSendMessage" when {
      val shortestPathSendMessage = PrivateMethod[Iterator[(VertexId, SimpleMSSG_PregelVertex)]]('shortestPathSendMessage)
//      val shortestPathSendMessage = PrivateMethod[Iterator[(VertexId, SPGraphData)]]('shortestPathSendMessage)
      "triplet where destination values dominate over any source values" should {
        "return an empty iterator" in {
          val srcAttr = Map[VertexId, SimpleMSSP_PregelMsg](
            (1L, SimpleMSSP_PregelMsg(5.0)),
            (2L, SimpleMSSP_PregelMsg(10.0)),
            (3L, SimpleMSSP_PregelMsg(15.0)),
            (4L, SimpleMSSP_PregelMsg(20.0)),
            (6L, SimpleMSSP_PregelMsg(30.0))
          )
          val dstAttr = Map[VertexId, SimpleMSSP_PregelMsg](
            (1L, SimpleMSSP_PregelMsg(5.0)),
            (2L, SimpleMSSP_PregelMsg(10.0)),
            (3L, SimpleMSSP_PregelMsg(15.0)),
            (4L, SimpleMSSP_PregelMsg(20.0)),
            (6L, SimpleMSSP_PregelMsg(30.0))
          )
          val graph = Graph[SimpleMSSG_PregelVertex, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, srcAttr),
              (2, dstAttr))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty())))
          )

          // triplets foreach used here because of limited API access to triplets
          // there will be exactly one triplet which will contain the exactly one edge in the graph
          graph.triplets.toLocalIterator.foreach(thisEdge => {
            val result: Iterator[(VertexId, SimpleMSSG_PregelVertex)] = SimpleMSSP invokePrivate shortestPathSendMessage(thisEdge)

            result.isEmpty should be (true)  // because src + edgeweight will always be 1 greater than dest
          })
        }
      }
      "triplet with some differences between source and destination values" should {
        "combine best of src + edge weight and destination values" in {
          val srcAttr = Map[VertexId, SimpleMSSP_PregelMsg](
            (1L, SimpleMSSP_PregelMsg(5.0)),
            (2L, SimpleMSSP_PregelMsg(10.0)),
            (3L, SimpleMSSP_PregelMsg(15.0)),
            (4L, SimpleMSSP_PregelMsg(20.0)),
            (6L, SimpleMSSP_PregelMsg(30.0))
          )
          val dstAttr = Map[VertexId, SimpleMSSP_PregelMsg](
            (1L, SimpleMSSP_PregelMsg(4.0)),
            (2L, SimpleMSSP_PregelMsg(8.0)),
            (3L, SimpleMSSP_PregelMsg(16.0)),
            (4L, SimpleMSSP_PregelMsg(32.0)),
            (5L, SimpleMSSP_PregelMsg(64.0))
          )
          val graph = Graph[SimpleMSSG_PregelVertex, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, srcAttr),
              (2, dstAttr))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty())))
          )

          // triplets foreach used here because of limited API access to triplets
          // there will be exactly one triplet which will contain the exactly one edge in the graph
          graph.triplets.toLocalIterator.foreach(thisEdge => {
            val result: Iterator[(VertexId, SimpleMSSG_PregelVertex)] = SimpleMSSP invokePrivate shortestPathSendMessage(thisEdge)

            val message = result.next._2
            message.getOrElse(1L, SimpleMSSP_PregelMsg()).weight should equal (4.0)  // destination attribute
            message.getOrElse(2L, SimpleMSSP_PregelMsg()).weight should equal (8.0)  // destination attribute
            message.getOrElse(3L, SimpleMSSP_PregelMsg()).weight should equal (16.0) // source attribute + edge weight
            message.getOrElse(4L, SimpleMSSP_PregelMsg()).weight should equal (21.0) // source attribute + edge weight
            message.getOrElse(5L, SimpleMSSP_PregelMsg()).weight should equal (64.0) // destination attribute
            message.getOrElse(6L, SimpleMSSP_PregelMsg()).weight should equal (31.0) // source attribute + edge weight
          })
        }
      }
    }

    "shortestPathMergeMessage" when {
      val shortestPathMergeMessage = PrivateMethod[SimpleMSSG_PregelVertex]('shortestPathMergeMessage)
      "called with two messages with the same keys but some different values" should {
        "take the smaller values from either message" in {
          val msg1 = Map[VertexId, SimpleMSSP_PregelMsg](
            (1L, SimpleMSSP_PregelMsg(5.0)),
            (2L, SimpleMSSP_PregelMsg(10.0)),
            (3L, SimpleMSSP_PregelMsg(15.0)),
            (4L, SimpleMSSP_PregelMsg(20.0)),
            (5L, SimpleMSSP_PregelMsg(25.0))
          )
          val msg2 = Map[VertexId, SimpleMSSP_PregelMsg](
            (1L, SimpleMSSP_PregelMsg(4.0)),
            (2L, SimpleMSSP_PregelMsg(8.0)),
            (3L, SimpleMSSP_PregelMsg(16.0)),
            (4L, SimpleMSSP_PregelMsg(32.0)),
            (5L, SimpleMSSP_PregelMsg(64.0))
          )

          val result: SimpleMSSG_PregelVertex = SimpleMSSP invokePrivate shortestPathMergeMessage(msg1, msg2)

          result.getOrElse(1L, SimpleMSSP_PregelMsg()).weight should equal (4.0)
          result.getOrElse(2L, SimpleMSSP_PregelMsg()).weight should equal (8.0)
          result.getOrElse(3L, SimpleMSSP_PregelMsg()).weight should equal (15.0)
          result.getOrElse(4L, SimpleMSSP_PregelMsg()).weight should equal (20.0)
          result.getOrElse(5L, SimpleMSSP_PregelMsg()).weight should equal (25.0)
        }
      }
      "called with two messages that have the same values" should {
        "return an equivalent message" in {
          val msg1 = Map[VertexId, SimpleMSSP_PregelMsg](
            (1L, SimpleMSSP_PregelMsg(5.0)),
            (2L, SimpleMSSP_PregelMsg(10.0)),
            (3L, SimpleMSSP_PregelMsg(15.0)),
            (4L, SimpleMSSP_PregelMsg(20.0)),
            (5L, SimpleMSSP_PregelMsg(25.0))
          )

          val result: SimpleMSSG_PregelVertex = SimpleMSSP invokePrivate shortestPathMergeMessage(msg1, msg1)

          result.getOrElse(1L, SimpleMSSP_PregelMsg()).weight should equal (5.0)
          result.getOrElse(2L, SimpleMSSP_PregelMsg()).weight should equal (10.0)
          result.getOrElse(3L, SimpleMSSP_PregelMsg()).weight should equal (15.0)
          result.getOrElse(4L, SimpleMSSP_PregelMsg()).weight should equal (20.0)
          result.getOrElse(5L, SimpleMSSP_PregelMsg()).weight should equal (25.0)
        }
      }
    }
  }
}
