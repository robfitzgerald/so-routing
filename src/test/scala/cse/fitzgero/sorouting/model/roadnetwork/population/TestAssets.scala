package cse.fitzgero.sorouting.model.roadnetwork.population

import java.time.LocalTime

import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.population._
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BasicCostFunction
import cse.fitzgero.sorouting.model.roadnetwork.local._

object TestAssets {
  trait TriangleWorld {
    val testTime = "12:34:56"
    val nowFormatted = LocalTime.parse(testTime)
    val validRequest = LocalRequest("joe", LocalODPair("joe", "1", "3"), nowFormatted)
    val validResponse = LocalResponse(validRequest, List(SORoutingPathSegment("102", Some(Seq(1.0))), SORoutingPathSegment("203", Some(Seq(1.0)))))

    val graph = LocalGraph(
      adjList = Map(
        "1" -> Map[String, String]("102" -> "2"),
        "2" -> Map[String, String]("203" -> "3"),
        "3" -> Map[String, String]("301" -> "1")
      ),
      edgeList = Map[String, LocalEdge](
        "102" -> LocalEdge("102", "1", "2", new LocalEdgeAttribute() with BasicCostFunction),
        "103" -> LocalEdge("103", "1", "3", new LocalEdgeAttribute() with BasicCostFunction),
        "203" -> LocalEdge("203", "2", "3", new LocalEdgeAttribute() with BasicCostFunction),
        "301" -> LocalEdge("301", "3", "1", new LocalEdgeAttribute() with BasicCostFunction)
      ),
      vertexList = Map[String, LocalVertex](
        "1" -> LocalVertex("1", -10, 0),
        "2" -> LocalVertex("2", 0, 10),
        "3" -> LocalVertex("3", 10, 0)
      )
    )
  }
}