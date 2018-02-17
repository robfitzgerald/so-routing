package cse.fitzgero.sorouting.algorithm.local.mssp

import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BasicCostFunction
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalEdge, LocalEdgeFlowAttribute, LocalGraph, LocalVertex}

object TestAssets {
  // this graph has a few alternate paths between vertex 1 and 10, and then wraps around from 10 to 1
  //
  val graph = LocalGraph (
    adjList = Map(
      "1" -> Map[String, String]("102" -> "2", "103" -> "3"),
      "2" -> Map[String, String]("201" -> "1", "203" -> "3", "204" -> "4"),
      "3" -> Map[String, String]("301" -> "1", "302" -> "2", "305" -> "5"),
      "4" -> Map[String, String]("406" -> "6"),
      "5" -> Map[String, String]("507" -> "7"),
      "6" -> Map[String, String]("608" -> "8", "610" -> "10"),
      "7" -> Map[String, String]("709" -> "9"),
      "8" -> Map[String, String]("810" -> "10"),
      "9" -> Map[String, String]("910" -> "10"),
      "10" -> Map("10-1" -> "1")
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
      "810" -> LocalEdge("810", "8", "10", new LocalEdgeFlowAttribute() with BasicCostFunction),
      "910" -> LocalEdge("910", "9", "10", new LocalEdgeFlowAttribute() with BasicCostFunction),
      "10-1" -> LocalEdge("10-1", "10", "1", new LocalEdgeFlowAttribute() with BasicCostFunction)
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
