package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BasicCostFunction

object TestAssets {
  trait LocalEdgeValidArgs1 {
    def id = "some ID"
    def src = "101"
    def dst = "505"
    def flow = Some(20D)
    def capacity = Some(100D)
    def freeFlowSpeed = Some(50D)
    def distance = Some(1000D)
  }
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
}
