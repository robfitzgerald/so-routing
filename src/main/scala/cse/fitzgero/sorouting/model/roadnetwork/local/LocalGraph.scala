package cse.fitzgero.sorouting.model.roadnetwork.local

import cse.fitzgero.graph.propertygraph._
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.CostFunction

import scala.collection.GenMap

// a graph class designed for local (non-cluster) simulations
class LocalGraph (
  adjList: GenMap[String, GenMap[String, String]],
  edgeList: GenMap[String, LocalEdge],
  vertexList: GenMap[String, LocalVertex]
) extends PropertyGraph with PropertyGraphOps with PropertyGraphMutationOps[LocalGraph] { graph =>

  override type VertexId = String
  override type EdgeId = String
  override type Vertex = LocalVertex
  override type Edge = LocalEdge

  def adjacencies: GenMap[String, GenMap[String, String]] = adjList
  def edges: GenMap[String, LocalEdge] = edgeList
  def vertices: GenMap[String, LocalVertex] = vertexList

  override def updateEdge(e: String, a: Edge): LocalGraph =
    if (adjList.isDefinedAt(a.src))
      new LocalGraph(
        adjList = adjList.updated(a.src, adjList(a.src).updated(e, a.dst)),
        edgeList = edgeList.updated(e, a),
        vertexList
      )
    else
      new LocalGraph(
        adjList = adjList.updated(a.src, Map(e -> a.dst)),
        edgeList = edgeList.updated(e, a),
        vertexList
      )

  override def updateVertex(v: String, a: LocalVertex): LocalGraph =
    if (adjList.isDefinedAt(v))
      new LocalGraph(
        adjList,
        edgeList,
        vertexList = vertexList.updated(v, a)
      )
    else
      new LocalGraph(
        adjList = adjList.updated(v, Map()),
        edgeList,
        vertexList = vertexList.updated(v, a)
      )

  override def edgeById(e: String): Option[Edge] =
    if (edgeList.isDefinedAt(e)) Some(edgeList(e))
    else None

  override def vertexById(v: String): Option[LocalVertex] =
    if (vertexList.isDefinedAt(v)) Some(vertexList(v))
    else None

  override def outEdges(v: String): Iterator[String] =
    if (adjList.isDefinedAt(v)) adjList(v).keys.iterator
    else Iterator.empty

  override def outEdgeOperation[A](
    default: A,
    v: String,
    getOp: (LocalEdge) => A,
    combineOp: (A, A) => A): A = {
    if (adjList.isDefinedAt(v))
      adjList(v)
        .keys
        .map(edgeList(_))
        .aggregate(default)(
          (sum, item) => {
            combineOp(sum, getOp(item))
          },
          (a, b) => {
            combineOp(a,b)
          }
        )
    else default
  }

  override def selectOutEdgeBy[A](
    v: String,
    selectOp: (Edge) => (String, A),
    compareOp: ((String, A), (String, A)) => (String, A)): Option[Edge] = {
    if (adjList.isDefinedAt(v)) {
      val bestEdgeTuple =
        adjList(v)
          .keys
          .map(edgeId => selectOp(edgeList(edgeId)))
          .reduce(compareOp)
      Some(edgeList(bestEdgeTuple._1))
    } else None
  }

  override def removeEdge(e: String): LocalGraph = {
    edgeById(e) match {
      case Some(edge) =>
        new LocalGraph(
          adjList = adjList.updated(edge.src, adjList(edge.src) - edge.id),
          edgeList = edgeList - edge.id,
          vertexList = vertexList
        )
      case None => graph
    }
  }

  // remove the vertex from the vertex list and adjacency list, as well as any edges that touch it
  override def removeVertex(v: String): LocalGraph = ???
}

object LocalGraph {
  def apply(): LocalGraph = new LocalGraph(Map(), Map(), Map())
  def apply(
    adjList: GenMap[String, GenMap[String, String]],
    edgeList: GenMap[String, LocalEdge],
    vertexList: GenMap[String, LocalVertex]
  ): LocalGraph = new LocalGraph(adjList, edgeList, vertexList)
  // TODO: add apply(edgeList, vertexList) that creates tuples that can be groupBy'd into an adjacency list
}