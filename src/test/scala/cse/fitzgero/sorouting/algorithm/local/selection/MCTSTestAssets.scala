package cse.fitzgero.sorouting.algorithm.local.selection

import java.time.LocalTime

import cse.fitzgero.sorouting.model.population.LocalRequest
import cse.fitzgero.sorouting.model.roadnetwork.costfunction._
import cse.fitzgero.sorouting.model.roadnetwork.local._

object MCTSTestAssets {

  trait graphComposedAlts extends requestComposed with graphComposedAltsTopology {
    val graph: LocalGraph =
      LocalGraph(
        adjList = adjList,
        vertexList = vertexList,
        edgeList = shortestPath ++ alts(None)
      )
  }

  trait requestComposed {
    val request: Seq[LocalRequest] =
      (1 to 9)
        .map {
          id =>
            LocalRequest(id.toString, LocalODPair(s"req-${id.toString}", "a", "d"), LocalTime.MIN)
        }
  }

  trait graphComposedAltsTopology {
    val adjList: Map[String, Map[String, String]] =
      Map(
        "a" -> Map("ab" -> "b", "ad" -> "d"),
        "b" -> Map("bc" -> "c", "bd" -> "d"),
        "c" -> Map("cd1" -> "d", "cd2" -> "d")
      )

    val vertexList: Map[String, LocalVertex] =
      Map[String, LocalVertex](
        "a" -> LocalVertex("a", 0, 0),
        "b" -> LocalVertex("b", 0, 50),
        "c" -> LocalVertex("c", 0, 100),
        "d" -> LocalVertex("d", 0, 150)
      )

    val shortestPath: Map[String, LocalEdge] =
      Map(
        "ab" -> LocalEdge("ab", "a", "b", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "bc" -> LocalEdge("bc", "b", "c", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "cd1" -> LocalEdge("cd1", "c", "d", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction)
      )

    def alts(flow: Option[Double]): Map[String, LocalEdge] =
      Map(
        "ad" -> LocalEdge("ad", "a", "d", new LocalEdgeAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "bd" -> LocalEdge("bd", "b", "d", new LocalEdgeAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "cd2" -> LocalEdge("cd2", "c", "d", new LocalEdgeAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction)
      )
  }

  // 9 requests over a map with 3 valid paths, each which should not increase too fast for < 4 people
  trait graphSquare9PeopleThreeWays extends graphSquareTopology with requestSquare {
    val graph =
      LocalGraph(
        adjList = adjList,
        vertexList = vertexList,
        edgeList = outerPathsEdges ++ tenElevenPaths(Some(Double.MaxValue)) ++ oneFivePath(None)
      )
  }
  
  trait requestSquare {
    val request: Seq[LocalRequest] =
    (1 to 9)
    .map {
          id =>
            LocalRequest(id.toString, LocalODPair(s"req-${id.toString}", "0", "9"), LocalTime.MIN)
        }
  }
  
  trait graphSquareTopology {
    val adjList: Map[String, Map[String, String]] = 
      Map(
        "0" -> Map("001" -> "1", "002" -> "2", "004" -> "4"),
        "1" -> Map("102" -> "2", "104" -> "4", "105" -> "5", "110" -> "10", "111" -> "11"),
        "2" -> Map("203" -> "3"),
        "3" -> Map("306" -> "6"),
        "4" -> Map("407" -> "7"),
        "5" -> Map("509" -> "9"),
        "6" -> Map("609" -> "9"),
        "7" -> Map("708" -> "8"),
        "8" -> Map("809" -> "9"),
        "9" -> Map(),
        "10" -> Map("10-9" -> "9"),
        "11" -> Map("11-9" -> "9")
      )
    val vertexList: Map[String, LocalVertex] = 
      Map[String, LocalVertex](
        "0" -> LocalVertex("0", -25, -25),
        "1" -> LocalVertex("1", 0, 0),
        "2" -> LocalVertex("2", 0, 50),
        "3" -> LocalVertex("3", 0, 100),
        "4" -> LocalVertex("4", 50, 0),
        "5" -> LocalVertex("5", 50, 50),
        "6" -> LocalVertex("6", 50, 100),
        "7" -> LocalVertex("7", 100, 0),
        "8" -> LocalVertex("8", 100, 50),
        "9" -> LocalVertex("9", 100, 100),
        "10" -> LocalVertex("10", 33, 66),
        "11" -> LocalVertex("11", 66, 33)
      )
    val outerPathsEdges: Map[String, LocalEdge] =
      Map[String, LocalEdge](
        "001" -> LocalEdge("001", "0", "1", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "002" -> LocalEdge("002", "0", "2", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "004" -> LocalEdge("004", "0", "4", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "102" -> LocalEdge("102", "1", "2", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "104" -> LocalEdge("104", "1", "4", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "203" -> LocalEdge("203", "2", "3", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "306" -> LocalEdge("306", "3", "6", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "407" -> LocalEdge("407", "4", "7", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "609" -> LocalEdge("609", "6", "9", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "708" -> LocalEdge("708", "7", "8", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "809" -> LocalEdge("809", "8", "9", new LocalEdgeAttribute(None, Some(4), Some(8.33), Some(50)) with BPRCostFunction)
      )
    def tenElevenPaths(flow: Option[Double]): Map[String, LocalEdge] =
      Map(
        "110" -> LocalEdge("110", "1", "10", new LocalEdgeAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "10-9" -> LocalEdge("10-9", "10", "9", new LocalEdgeAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "111" -> LocalEdge("111", "1", "11", new LocalEdgeAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "11-9" -> LocalEdge("11-9", "11", "9", new LocalEdgeAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction)
    )
    def oneFivePath(flow: Option[Double]): Map[String, LocalEdge] =
      Map(
        "105" -> LocalEdge("105", "1", "5", new LocalEdgeAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction),
        "509" -> LocalEdge("509", "5", "9", new LocalEdgeAttribute(flow, Some(4), Some(8.33), Some(50)) with BPRCostFunction)
      )
  }
  
}
