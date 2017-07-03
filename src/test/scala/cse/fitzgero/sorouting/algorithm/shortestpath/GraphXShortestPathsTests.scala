package cse.fitzgero.sorouting.algorithm.shortestpath

import org.apache.spark.graphx._
import cse.fitzgero.sorouting.SparkUnitTestTemplate
import cse.fitzgero.sorouting.matsimrunner.population.PersonIDType
import cse.fitzgero.sorouting.roadnetwork.costfunction._
import cse.fitzgero.sorouting.roadnetwork.edge._
import cse.fitzgero.sorouting.roadnetwork.vertex._
import cse.fitzgero.sorouting.roadnetwork.graph._

class GraphXShortestPathsTests extends SparkUnitTestTemplate("GraphXShortestPathsTests") {
  val Infinity: Double = Double.PositiveInfinity
  val equilNetworkFilePath: String =  "src/test/resources/GraphXShortestPathsTests/network-matsim-example-equil.xml"
  val equilSnapshotFilePath: String = "src/test/resources/GraphXShortestPathsTests/snapshot-matsim-example-equil.xml"
  "GraphXShortestPathsTests" when {
    "shortestPaths" when {
      "given a pair of vertex ids and a graph with no congestion and a constant-valued cost function" should {
        "return the correct shortest path, which should be shortest by number of links" in {
          val graph: RoadNetwork = GraphXMacroRoadNetwork(sc, TestCostFunction).fromFile(equilNetworkFilePath).get
          val odPairs: ODPairs = Seq(ODPair("1",1,12), ODPair("2",3,15), ODPair("3",12,1), ODPair("4",2, 3))
ODPair
          val result: ODPaths = GraphXShortestPaths.shortestPaths(graph, odPairs)

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
          val odPairs: ODPairs = Seq(ODPair("1",1,12), ODPair("2",3,15), ODPair("3",12,1), ODPair("4",2, 3))

          val result: ODPaths = GraphXShortestPaths.shortestPaths(graph, odPairs)

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
          val odPairs: ODPairs = Seq(ODPair("1234",3,1))

          val paths: ODPaths = GraphXShortestPaths.shortestPaths(graph, odPairs)
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
          val odPairs: ODPairs = Seq(ODPair("1",1,12), ODPair("2",3,15), ODPair("3",12,1), ODPair("4",2, 3))

          val result: ODPaths = GraphXShortestPaths.shortestPaths(graph, odPairs, AONFlow())

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
      val runPregelShortestPaths = PrivateMethod[Graph[SPGraphData, MacroscopicEdgeProperty]]('runPregelShortestPaths)
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
          val odPairs: ODPairs = Seq(ODPair("1",1,3))

          val result = GraphXShortestPaths invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SPGraphData] = result.vertices.toLocalIterator.toMap

          resultLocal(1)(1).weight should equal(0.0)
          resultLocal(2)(1).weight should equal(1.0)
          resultLocal(3)(1).weight should equal(2.0)
        }
      }
      "run with a 15-intersection road network and default flow costs of 1" should {
        "return a shortest paths graph" in {
          val graph: RoadNetwork = GraphXMacroRoadNetwork(sc, TestCostFunction).fromFile(equilNetworkFilePath).get
          val odPairs: ODPairs = Seq(ODPair("1",1,12), ODPair("11",3,15), ODPair("2",6,1))

          val result = GraphXShortestPaths invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SPGraphData] = result.vertices.toLocalIterator.toMap

          val correctSolution: Map[VertexId, Map[VertexId, SPGraphMsg]] =
            Map((5,Map(1L -> SPGraphMsg("1",2.0), 3L -> SPGraphMsg("2",7.0), 6L -> SPGraphMsg("3",7.0))),
            (10,Map(1L -> SPGraphMsg("1",2.0), 3L -> SPGraphMsg("2",7.0), 6L -> SPGraphMsg("3",7.0))),
            (14,Map(1L -> SPGraphMsg("1",5.0), 3L -> SPGraphMsg("2",3.0), 6L -> SPGraphMsg("3",3.0))),
            (1,Map(1L -> SPGraphMsg("1",0.0), 3L -> SPGraphMsg("2",5.0), 6L -> SPGraphMsg("3",5.0))),
            (6,Map(1L -> SPGraphMsg("1",2.0), 3L -> SPGraphMsg("2",7.0), 6L -> SPGraphMsg("3",0.0))),
            (9,Map(1L -> SPGraphMsg("1",2.0), 3L -> SPGraphMsg("2",7.0), 6L -> SPGraphMsg("3",7.0))),
            (13,Map(1L -> SPGraphMsg("1",4.0), 3L -> SPGraphMsg("2",2.0), 6L -> SPGraphMsg("3",2.0))),
            (2,Map(1L -> SPGraphMsg("1",1.0), 3L -> SPGraphMsg("2",6.0), 6L -> SPGraphMsg("3",6.0))),
            (12,Map(1L -> SPGraphMsg("1",3.0), 3L -> SPGraphMsg("2",1.0), 6L -> SPGraphMsg("3",1.0))),
            (7,Map(1L -> SPGraphMsg("1",2.0), 3L -> SPGraphMsg("2",7.0), 6L -> SPGraphMsg("3",7.0))),
            (3,Map(1L -> SPGraphMsg("1",2.0), 3L -> SPGraphMsg("2",0.0), 6L -> SPGraphMsg("3",7.0))),
            (11,Map(1L -> SPGraphMsg("1",2.0), 3L -> SPGraphMsg("2",7.0), 6L -> SPGraphMsg("3",7.0))),
            (8,Map(1L -> SPGraphMsg("1",2.0), 3L -> SPGraphMsg("2",7.0), 6L -> SPGraphMsg("3",7.0))),
            (4,Map(1L -> SPGraphMsg("1",2.0), 3L -> SPGraphMsg("2",7.0), 6L -> SPGraphMsg("3",7.0))),
            (15,Map(1L -> SPGraphMsg("1",6.0), 3L -> SPGraphMsg("2",4.0), 6L -> SPGraphMsg("3",4.0)))
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
          val odPairs: ODPairs = Seq(ODPair("2",1,12), ODPair("@4",3,15), ODPair("45",6,1))

          val result = GraphXShortestPaths invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SPGraphData] = result.vertices.toLocalIterator.toMap

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
          val odPairs: ODPairs = Seq(ODPair("a",3,1))

          val result = GraphXShortestPaths invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SPGraphData] = result.vertices.toLocalIterator.toMap

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
          val odPairs: ODPairs = Seq.empty[ODPair]

          val result = GraphXShortestPaths invokePrivate runPregelShortestPaths(graph, odPairs)
          val resultLocal: Map[VertexId, SPGraphData] = result.vertices.toLocalIterator.toMap

          resultLocal(1)(3).weight should equal(Infinity)
          resultLocal(2)(3).weight should equal(Infinity)
          resultLocal(3)(3).weight should equal(Infinity)
        }
      }
    }
    "initialShorestPathsMessage" when {
      val initialShorestPathsMessage = PrivateMethod[Map[VertexId, SPGraphMsg]]('initialShorestPathsMessage)
      "passed a set of origin-destination pairs" should {
        "return a map of those destinations to default weights (infinity)" in {
          val odPairs: ODPairs = Seq(ODPair("a",1,10), ODPair("a",2,20), ODPair("b",3,30))
          val result: Map[VertexId, SPGraphMsg] = GraphXShortestPaths invokePrivate initialShorestPathsMessage(odPairs)

          Seq(1, 2, 3).foreach(v => {
            result(v).weight should equal (Double.PositiveInfinity)
            result(v).path should equal (List())
          })
        }
        "produces a map with a default value of Infinity" in {
          val odPairs: ODPairs = Seq(ODPair("a",1,10), ODPair("a",2,20), ODPair("b",3,30))
          val result: Map[VertexId, SPGraphMsg] = GraphXShortestPaths invokePrivate initialShorestPathsMessage(odPairs)

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
          val graph: RoadNetwork = Graph[CoordinateVertexProperty, MacroscopicEdgeProperty](
            sc.parallelize(List(
              (1, CoordinateVertexProperty(Euclidian(0,0))),
              (2, CoordinateVertexProperty(Euclidian(0,0))),
              (3, CoordinateVertexProperty(Euclidian(0,0))))),
            sc.parallelize(List(
              Edge(1,2,MacroscopicEdgeProperty("1")),
              Edge(2,3,MacroscopicEdgeProperty("2")),
              Edge(3,1,MacroscopicEdgeProperty("3")))))
          val odPairs: ODPairs = Seq(ODPair("1",1,3), ODPair("2",2,1), ODPair("3",3,2))

          val result = GraphXShortestPaths invokePrivate initializeShortestPathsGraph(graph, odPairs)

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
          val thisVertex = Map[VertexId, SPGraphMsg]((sourceId, SPGraphMsg("a",3.14159)))
          val incomingMessage: Map[VertexId, SPGraphMsg] = Map((sourceId, SPGraphMsg("b",7.5)))

          val result: SPGraphData = GraphXShortestPaths invokePrivate shortestPathVertexProgram(thisId, thisVertex, incomingMessage)

          result.get(sourceId) should equal (thisVertex.get(sourceId))
        }
      }
      "called with the vertexId, local information, and summed neighbor messages" should {
        "update the vertex pathDistances with the new shortest path data" in {
          val sourceId: VertexId = 1L
          val thisId: VertexId = 42L // an arbitrary vertexId in the graph
          val thisVertex = Map[VertexId, SPGraphMsg]((sourceId, SPGraphMsg("a",7.5)))
          val incomingMessage: Map[VertexId, SPGraphMsg] = Map((sourceId, SPGraphMsg("b",3.14159)))

          val result: SPGraphData = GraphXShortestPaths invokePrivate shortestPathVertexProgram(thisId, thisVertex, incomingMessage)

          result.get(sourceId) should equal (incomingMessage.get(sourceId))
        }
      }
      "called with multiple source locations" should {
        "only update the shorter values" in {
          val thisId: VertexId = 42L // an arbitrary vertexId in the graph
          val thisVertex = Map[VertexId, SPGraphMsg](
            (1L, SPGraphMsg("b",5.0)),
            (2L, SPGraphMsg("b",10.0)),
            (3L, SPGraphMsg("b",15.0))
          )
          val incomingMessage: Map[VertexId, SPGraphMsg] = Map(
            (1L, SPGraphMsg("b",4.0)),
            (2L, SPGraphMsg("b",8.0)),
            (3L, SPGraphMsg("b",16.0))
          )

          val result: SPGraphData = GraphXShortestPaths invokePrivate shortestPathVertexProgram(thisId, thisVertex, incomingMessage)

          result.getOrElse(1L, SPGraphMsg("B")).weight should equal (4.0)
          result.getOrElse(2L, SPGraphMsg("B")).weight should equal (8.0)
          result.getOrElse(3L, SPGraphMsg("B")).weight should equal (15.0)
        }
      }
    }

    "shortestPathSendMessage" when {
      val shortestPathSendMessageWrapper = PrivateMethod[(EdgeTriplet[SPGraphData, MacroscopicEdgeProperty]) => Iterator[(VertexId, SPGraphData)]]('shortestPathSendMessageWrapper)
//      val shortestPathSendMessage = PrivateMethod[Iterator[(VertexId, SPGraphData)]]('shortestPathSendMessage)
      "triplet where destination values dominate over any source values" should {
        "return an empty iterator" in {
          val srcAttr = Map[VertexId, SPGraphMsg](
            (1L, SPGraphMsg("a",5.0)),
            (2L, SPGraphMsg("a",10.0)),
            (3L, SPGraphMsg("a",15.0)),
            (4L, SPGraphMsg("a",20.0)),
            (6L, SPGraphMsg("a",30.0))
          )
          val dstAttr = Map[VertexId, SPGraphMsg](
            (1L, SPGraphMsg("a",5.0)),
            (2L, SPGraphMsg("a",10.0)),
            (3L, SPGraphMsg("a",15.0)),
            (4L, SPGraphMsg("a",20.0)),
            (6L, SPGraphMsg("a",30.0))
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
            val setup = GraphXShortestPaths invokePrivate shortestPathSendMessageWrapper(CostFlow())
            val result: Iterator[(VertexId, SPGraphData)] = setup(thisEdge)

            result.isEmpty should be (true)  // because src + edgeweight will always be 1 greater than dest
          })
        }
      }
      "triplet with some differences between source and destination values" should {
        "combine best of src + edge weight and destination values" in {
          val srcAttr = Map[VertexId, SPGraphMsg](
            (1L, SPGraphMsg("b",5.0)),
            (2L, SPGraphMsg("b",10.0)),
            (3L, SPGraphMsg("b",15.0)),
            (4L, SPGraphMsg("b",20.0)),
            (6L, SPGraphMsg("b",30.0))
          )
          val dstAttr = Map[VertexId, SPGraphMsg](
            (1L, SPGraphMsg("b",4.0)),
            (2L, SPGraphMsg("b",8.0)),
            (3L, SPGraphMsg("b",16.0)),
            (4L, SPGraphMsg("b",32.0)),
            (5L, SPGraphMsg("b",64.0))
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
            val setup = GraphXShortestPaths invokePrivate shortestPathSendMessageWrapper(CostFlow())
            val result: Iterator[(VertexId, SPGraphData)] = setup(thisEdge)

            val message = result.next._2
            message.getOrElse(1L, SPGraphMsg("a")).weight should equal (4.0)  // destination attribute
            message.getOrElse(2L, SPGraphMsg("a")).weight should equal (8.0)  // destination attribute
            message.getOrElse(3L, SPGraphMsg("a")).weight should equal (16.0) // source attribute + edge weight
            message.getOrElse(4L, SPGraphMsg("a")).weight should equal (21.0) // source attribute + edge weight
            message.getOrElse(5L, SPGraphMsg("a")).weight should equal (64.0) // destination attribute
            message.getOrElse(6L, SPGraphMsg("a")).weight should equal (31.0) // source attribute + edge weight
          })
        }
      }
    }

    "shortestPathMergeMessage" when {
      val shortestPathMergeMessage = PrivateMethod[SPGraphData]('shortestPathMergeMessage)
      "called with two messages with the same keys but some different values" should {
        "take the smaller values from either message" in {
          val msg1 = Map[VertexId, SPGraphMsg](
            (1L, SPGraphMsg("a",5.0)),
            (2L, SPGraphMsg("a",10.0)),
            (3L, SPGraphMsg("a",15.0)),
            (4L, SPGraphMsg("a",20.0)),
            (5L, SPGraphMsg("a",25.0))
          )
          val msg2 = Map[VertexId, SPGraphMsg](
            (1L, SPGraphMsg("a",4.0)),
            (2L, SPGraphMsg("a",8.0)),
            (3L, SPGraphMsg("a",16.0)),
            (4L, SPGraphMsg("a",32.0)),
            (5L, SPGraphMsg("a",64.0))
          )

          val result: SPGraphData = GraphXShortestPaths invokePrivate shortestPathMergeMessage(msg1, msg2)

          result.getOrElse(1L, SPGraphMsg("a")).weight should equal (4.0)
          result.getOrElse(2L, SPGraphMsg("a")).weight should equal (8.0)
          result.getOrElse(3L, SPGraphMsg("a")).weight should equal (15.0)
          result.getOrElse(4L, SPGraphMsg("a")).weight should equal (20.0)
          result.getOrElse(5L, SPGraphMsg("a")).weight should equal (25.0)
        }
      }
      "called with two messages that have the same values" should {
        "return an equivalent message" in {
          val msg1 = Map[VertexId, SPGraphMsg](
            (1L, SPGraphMsg("b",5.0)),
            (2L, SPGraphMsg("b",10.0)),
            (3L, SPGraphMsg("b",15.0)),
            (4L, SPGraphMsg("b",20.0)),
            (5L, SPGraphMsg("b",25.0))
          )

          val result: SPGraphData = GraphXShortestPaths invokePrivate shortestPathMergeMessage(msg1, msg1)

          result.getOrElse(1L, SPGraphMsg("B")).weight should equal (5.0)
          result.getOrElse(2L, SPGraphMsg("B")).weight should equal (10.0)
          result.getOrElse(3L, SPGraphMsg("B")).weight should equal (15.0)
          result.getOrElse(4L, SPGraphMsg("B")).weight should equal (20.0)
          result.getOrElse(5L, SPGraphMsg("B")).weight should equal (25.0)
        }
      }
    }
  }
}
