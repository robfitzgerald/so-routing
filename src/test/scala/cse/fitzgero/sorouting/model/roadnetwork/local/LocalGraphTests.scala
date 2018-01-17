package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class LocalGraphTests extends SORoutingUnitTestTemplate {
  "LocalGraph" when {
    "removeEdge" should {
      "remove the edge from the edge list and adjacency list" in new TestAssets.TriangleWorld {
        val result = graph.removeEdge("203")
        result.adjacencies("2").isEmpty should be (true)
        result.edges.isDefinedAt("203") should be (false)
      }
    }
    "removeVertex" should {
      "remove the vertex from the vertex list and adjacency list, as well as any edges that touch it" in {

      }
    }
  }
}
