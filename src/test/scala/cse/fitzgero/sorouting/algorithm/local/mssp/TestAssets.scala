package cse.fitzgero.sorouting.algorithm.local.mssp

import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalEdge, LocalEdgeAttributeBasic, LocalGraph, LocalVertex}

object TestAssets {
  val graph = LocalGraph (
    adjList = Map(
      "1" -> Map[String, String]("102" -> "2", "103" -> "3"),
      "2" -> Map[String, String]("201" -> "1", "203" -> "3", "204" -> "4"),
      "3" -> Map[String, String]("301" -> "1", "302" -> "2", "305" -> "5"),
      "4" -> Map[String, String]("406" -> "6"),
      "5" -> Map[String, String]("507" -> "7"),
      "6" -> Map[String, String]("608" -> "8", "610" -> "10"),
      "7" -> Map[String, String]("709" -> "9"),
      "8" -> Map.empty[String, String],
      "9" -> Map[String, String]("910" -> "10"),
      "10" -> Map("10-1" -> "1")
    ),
    edgeList = Map[String, LocalEdge](
      "102" -> LocalEdge("102", "1", "2", LocalEdgeAttributeBasic()),
      "103" -> LocalEdge("103", "1", "3", LocalEdgeAttributeBasic()),
      "201" -> LocalEdge("201", "2", "1", LocalEdgeAttributeBasic()),
      "203" -> LocalEdge("203", "2", "3", LocalEdgeAttributeBasic()),
      "204" -> LocalEdge("204", "2", "4", LocalEdgeAttributeBasic()),
      "301" -> LocalEdge("301", "3", "1", LocalEdgeAttributeBasic()),
      "302" -> LocalEdge("302", "3", "2", LocalEdgeAttributeBasic()),
      "305" -> LocalEdge("305", "3", "5", LocalEdgeAttributeBasic()),
      "406" -> LocalEdge("406", "4", "6", LocalEdgeAttributeBasic()),
      "507" -> LocalEdge("507", "5", "7", LocalEdgeAttributeBasic()),
      "608" -> LocalEdge("608", "6", "8", LocalEdgeAttributeBasic()),
      "610" -> LocalEdge("610", "6", "10", LocalEdgeAttributeBasic()),
      "709" -> LocalEdge("709", "7", "9", LocalEdgeAttributeBasic()),
      "910" -> LocalEdge("910", "9", "10", LocalEdgeAttributeBasic()),
      "10-1" -> LocalEdge("10-1", "10", "1", LocalEdgeAttributeBasic())
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
