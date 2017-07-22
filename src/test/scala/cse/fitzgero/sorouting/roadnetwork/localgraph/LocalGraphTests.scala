package cse.fitzgero.sorouting.roadnetwork.localgraph

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class LocalGraphTests extends SORoutingUnitTestTemplate {
  "LocalGraph" when {
    trait TestGraph {
      val graph = new LocalGraph[String, Double](
        Map[VertexId, Map[EdgeId, VertexId]](
          1L -> Map(10L -> 2L),
          2L -> Map(11L -> 1L, 12L -> 3L),
          3L -> Map(13L -> 1L)
        ),
        Map[VertexId, String](
          1L -> "1",
          2L -> "2",
          3L -> "3"
        ),
        Map[EdgeId, Double](
          10L -> 10.0D,
          11L -> 11.0D,
          12L -> 12.0D,
          13L -> 13.0D
        )
      )
    }
    "edgeAttrOf" when {
      "called with a valid edge id" should {
        "give us the edge attribute corresponding to that edge id" in {
          new TestGraph {
            graph.edgeAttrOf(10L).get should equal (10.0D)
          }
        }
      }
      "called with an invalid edge id" should {
        "return None" in {
          new TestGraph {
            graph.edgeAttrOf(123456L) should equal (None)
          }
        }
      }
    }
    "vertexAttrOf" when {
      "called with a valid vertex id" should {
        "give us the vertex attribute corresponding to that vertex id" in {
          new TestGraph {
            graph.vertexAttrOf(1L).get should equal ("1")
          }
        }
      }
      "called with an invalid vertex id" should {
        "return None" in {
          new TestGraph {
            graph.vertexAttrOf(123456L) should equal (None)
          }
        }
      }
    }
    "neighborTriplets" when {
      "called with a valid vertex id" should {
        "give us an iterator of the adjacent/incident v-e->v tuples" in {
          new TestGraph {
            val result: Iterator[graph.Triplet] = graph.neighborTriplets(2L)
            result.hasNext should equal (true)
            result.next should equal (graph.Triplet(2L, 11L, 1L))
            result.hasNext should equal (true)
            result.next should equal (graph.Triplet(2L, 12L, 3L))
            result.isEmpty should equal (true)
          }
        }
      }
      "called with an invalid vertex id" should {
        "return an empty iterator" in {
          new TestGraph {
            val result: Iterator[graph.Triplet] = graph.neighborTriplets(2351346L)
            result.isEmpty should be (true)
          }
        }
      }
    }
    "neighborTripletAttrs" when {
      "called with a valid vertex id" should {
        "give us an iterator of the adjacent/incident v-e->v tuples in the form of their attributes" in {
          new TestGraph {
            val result: Iterator[graph.TripletAttrs] = graph.neighborTripletAttrs(2L)
            result.hasNext should equal (true)
            result.next should equal (graph.TripletAttrs("2", 11.0D, "1"))
            result.hasNext should equal (true)
            result.next should equal (graph.TripletAttrs("2", 12.0D, "3"))
            result.isEmpty should equal (true)
          }
        }
      }
      "called with an invalid vertex id" should {
        "return an empty iterator" in {
          new TestGraph {
            val result: Iterator[graph.TripletAttrs] = graph.neighborTripletAttrs(2351346L)
            result.isEmpty should be (true)
          }
        }
      }
    }
    "neighborEdgeAttrs" when {
      "called with a valid vertex id" should {
        "give us an iterator of the incident edges in the form of their attributes" in {
          new TestGraph {
            val result: Iterator[Double] = graph.incidentEdgeAttrs(2L)
            result.hasNext should equal (true)
            result.next should equal (11.0D)
            result.hasNext should equal (true)
            result.next should equal (12.0D)
            result.isEmpty should equal (true)
          }
        }
      }
      "called with an invalid vertex id" should {
        "return an empty iterator" in {
          new TestGraph {
            val result: Iterator[Double] = graph.incidentEdgeAttrs(2351346L)
            result.isEmpty should be (true)
          }
        }
      }
    }
    "updateVertex" when {
      "called with a new VertexId and an attribute" should {
        "return the graph updated with the new attribute" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.updateVertex(4L, "4")
            result.vertices.toList should equal (List(1L,2L,3L,4L))
            result.vertices.foreach(v=> {
              result.vertexAttrOf(v).get should equal (v.toString)
            })
          }
        }
      }
      "called with an attribute at a pre-existing id" should {
        "over write the attribute at that id" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.updateVertex(2L, "3").updateVertex(3L, "5")
            result.vertices.foreach(v => {
              result.vertexAttrOf(v).get should equal (((2 *v) - 1).toString)
            })
          }
        }
      }
      "calling addVertex (alias for updateVertex)" should {
        "add a vertex to an empty LocalGraph" in {
          val graph = LocalGraph[String, Double]()
          val result = graph.addVertex(1L, "1")
          result.vertices.toList should equal (List(1L))
          result.vertexAttrOf(1L).get should equal ("1")
        }
      }
    }
    "deleteVertex" when {
      "called at a pre-existing id" should {
        "removes vertex at this id" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.deleteVertex(2L)
            result.vertices.toSeq should equal(Seq(1L,3L))
            result.vertices.foreach(v => {
              result.vertexAttrOf(v).get should equal (v.toString)
            })
          }
        }
      }
      "called at a non-existing id" should {
        "have no effect" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.deleteVertex(5L)
            result.vertices.toSeq should equal(Seq(1L, 2L, 3L))
            result.vertices.foreach(v => {
              result.vertexAttrOf(v).get should equal (v.toString)
            })
          }
        }
      }
    }
    "updateEdge" when {
      "called with a new EdgeId and an attribute" should {
        "return the graph updated with the new attribute" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.updateEdge(14L, 14.0D)
            result.edges.foreach(e=> {
              val edgeAttr = result.edgeAttrOf(e).get
              edgeAttr should equal (e.toDouble)
            })
          }
        }
      }
      "called with an attribute at a pre-existing id" should {
        "over write the attribute at that id" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.updateEdge(10L, 0D).updateEdge(11L, 1D).updateEdge(12L, 2D).updateEdge(13L, 3D)
            result.edges.foreach(e => {
              val edgeAttr = result.edgeAttrOf(e).get
              edgeAttr should equal ((e - 10).toDouble)
            })
          }
        }
      }
    }
    "deleteEdge" when {
      "called at a pre-existing id" should {
        "removes edge at this id" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.deleteEdge(11L)
            result.edges.toSeq should equal(Seq(10L,12L,13L))
            result.edges.foreach(e => {
              result.edgeAttrOf(e).get should equal (e.toDouble)
            })
          }
        }
      }
      "called at a non-existing id" should {
        "have no effect" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.deleteVertex(5L)
            result.edges.toSeq should equal(Seq(10L, 11L, 12L, 13L))
            result.edges.foreach(e => {
              result.edgeAttrOf(e).get should equal (e.toDouble)
            })
          }
        }
      }
    }
    "addEdge" when {
      "called at a pre-existing id" should {
        "overwrite the edge attribute at that id but not change the adjacency matrix" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.addEdge(graph.Triplet(1L, 10L, 2L), 1234.0D)
            result.edges.toSeq should equal(Seq(10L,11L,12L,13L))
            result.edgeAttrOf(10L).get should equal (1234.0D)
            result.adjacencyList should equal (graph.adjacencyList)
          }
        }
      }
      "called at a non-existing id" should {
        "create an edge and connect the associated vertices in the adjacency list" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.addEdge(graph.Triplet(1L, 14L, 3L), 14.0D)
            result.edges.toSeq.sorted should equal(Seq(10L, 11L, 12L, 13L, 14L))
            result.edges.foreach(e => {
              result.edgeAttrOf(e).get should equal (e.toDouble)
            })
          }
        }
      }
      "called at a non-existing id but with a non-existing vertex" should {
        "make no change" in {
          new TestGraph {
            val result: LocalGraph[String, Double] = graph.addEdge(graph.Triplet(100L, 14L, 3L), 14.0D)
            result should equal (graph)
          }
        }
      }
    }
  }
}
