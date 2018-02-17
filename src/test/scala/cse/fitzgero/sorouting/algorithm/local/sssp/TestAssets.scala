package cse.fitzgero.sorouting.algorithm.local.sssp

import cse.fitzgero.sorouting.algorithm.local.sssp.SSSPLocalDijkstrasService._
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BasicCostFunction
import cse.fitzgero.sorouting.model.roadnetwork.local._

object TestAssets {
  trait TriangleWorld {
    val graph = LocalGraph(
      adjList = Map(
        "1" -> Map[String, String]("102" -> "2", "103" -> "3"),
        "2" -> Map[String, String]("203" -> "3"),
        "3" -> Map[String, String]("301" -> "1")
      ),
      edgeList = Map[String, LocalEdge](
        "102" -> LocalEdge("102", "1", "2", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "103" -> LocalEdge("103", "1", "3", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "203" -> LocalEdge("203", "2", "3", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "301" -> LocalEdge("301", "3", "1", new LocalEdgeFlowAttribute() with BasicCostFunction)
      ),
      vertexList = Map[String, LocalVertex](
        "1" -> LocalVertex("1", -10, 0),
        "2" -> LocalVertex("2", 0, 10),
        "3" -> LocalVertex("3", 10, 0)
      )
    )
  }
  trait K5Graph {
    val graph = LocalGraph(
      adjList = Map(
        "1" -> Map[String, String]("102" -> "2", "103" -> "3", "104" -> "4", "105" -> "5"),
        "2" -> Map[String, String]("201" -> "1", "203" -> "3", "204" -> "4", "205" -> "5"),
        "3" -> Map[String, String]("301" -> "1", "302" -> "2", "304" -> "4", "305" -> "5"),
        "4" -> Map[String, String]("401" -> "1", "402" -> "2", "403" -> "3", "405" -> "5"),
        "5" -> Map[String, String]("501" -> "1", "502" -> "2", "503" -> "3", "504" -> "4")
      ),
      edgeList = Map[String, LocalEdge](
        "102" -> LocalEdge("102", "1", "2", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "103" -> LocalEdge("103", "1", "3", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "104" -> LocalEdge("104", "1", "4", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "105" -> LocalEdge("105", "1", "5", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "201" -> LocalEdge("201", "2", "1", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "203" -> LocalEdge("203", "2", "3", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "204" -> LocalEdge("204", "2", "4", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "205" -> LocalEdge("205", "2", "5", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "301" -> LocalEdge("301", "3", "1", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "302" -> LocalEdge("302", "3", "2", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "304" -> LocalEdge("304", "3", "4", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "305" -> LocalEdge("305", "3", "5", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "401" -> LocalEdge("401", "4", "1", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "402" -> LocalEdge("402", "4", "2", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "403" -> LocalEdge("403", "4", "3", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "405" -> LocalEdge("405", "4", "5", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "501" -> LocalEdge("501", "5", "1", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "502" -> LocalEdge("502", "5", "2", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "503" -> LocalEdge("503", "5", "3", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "504" -> LocalEdge("504", "5", "4", new LocalEdgeFlowAttribute() with BasicCostFunction)
      ),
      vertexList = Map[String, LocalVertex](
        "1" -> LocalVertex("1", -10, 0),
        "2" -> LocalVertex("2", 0, 10),
        "3" -> LocalVertex("3", 10, 0),
        "4" -> LocalVertex("4", 10, 0),
        "5" -> LocalVertex("5", 10, 0)
      )
    )
  }
  trait TwoPathsGraph {
    val graph = LocalGraph(
      adjList = Map(
        "1" -> Map[String, String]("102" -> "2", "103" -> "3"),
        "2" -> Map[String, String]("201" -> "1", "203" -> "3", "204" -> "4"),
        "3" -> Map[String, String]("301" -> "1", "302" -> "2", "305" -> "5"),
        "4" -> Map[String, String]("406" -> "6"),
        "5" -> Map[String, String]("507" -> "7"),
        "6" -> Map[String, String]("608" -> "8", "610" -> "10"),
        "7" -> Map[String, String]("709" -> "9"),
        "8" -> Map.empty[String, String],
        "9" -> Map[String, String]("910" -> "10")
      ),
      edgeList = Map[String, LocalEdge](
        "102" -> LocalEdge("102", "1", "2", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "103" -> LocalEdge("103", "1", "3", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "201" -> LocalEdge("201", "2", "1", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "203" -> LocalEdge("203", "2", "3", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "204" -> LocalEdge("204", "2", "4", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "301" -> LocalEdge("301", "3", "1", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "302" -> LocalEdge("302", "3", "2", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "305" -> LocalEdge("305", "3", "5", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "406" -> LocalEdge("406", "4", "6", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "507" -> LocalEdge("507", "5", "7", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "608" -> LocalEdge("608", "6", "8", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "610" -> LocalEdge("610", "6", "10", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "709" -> LocalEdge("709", "7", "9", new LocalEdgeFlowAttribute() with BasicCostFunction),
        "910" -> LocalEdge("910", "9", "10", new LocalEdgeFlowAttribute() with BasicCostFunction)
      ),
      vertexList = Map[String, LocalVertex](
        "1" -> LocalVertex("1", -10, 0),
        "2" -> LocalVertex("2", 0, 10),
        "3" -> LocalVertex("3", 10, 0),
        "4" -> LocalVertex("4", 10, 0),
        "5" -> LocalVertex("5", 10, 0),
        "6" -> LocalVertex("3", 10, 0),
        "7" -> LocalVertex("4", 10, 0),
        "8" -> LocalVertex("5", 10, 0),
        "9" -> LocalVertex("4", 10, 0),
        "10" -> LocalVertex("5", 10, 0)
      )
    )
  }

  import SSSPLocalDijkstrasAlgorithm.BackPropagateData

  trait BackPropData {
    val spanning = Map(
      "1"-> BackPropagateData(None,0.0),
      "2"-> BackPropagateData(Some("102"),1.0),
      "3"-> BackPropagateData(Some("103"),1.0),
      "4" -> BackPropagateData(Some("204"),2.0),
      "5"-> BackPropagateData(Some("305"),2.0),
      "6"-> BackPropagateData(Some("406"),3.0),
      "7"-> BackPropagateData(Some("507"),3.0),
      "10"-> BackPropagateData(Some("610"),4.0)
    )
  }
}
