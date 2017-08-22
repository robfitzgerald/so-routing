package cse.fitzgero.sorouting.roadnetwork.localgraph

import cse.fitzgero.sorouting.roadnetwork.RoadNetwork

import scala.collection.{GenIterable, GenMap}


class LocalGraph [V, E] private[localgraph]
  (adj: GenMap[VertexId, Map[EdgeId, VertexId]], _vAttr: GenMap[VertexId, V], _eAttr: GenMap[EdgeId, E])
  extends RoadNetwork {

  case class TripletAttrs(o: V, e: E, d: V)

  def adjacencyList: GenMap[VertexId, Map[EdgeId, VertexId]] = adj
  def vertices: GenIterable[VertexId] = _vAttr.keys
  def edges: GenIterable[EdgeId] = _eAttr.keys
  def edgeAttrs: GenIterable[E] = _eAttr.values
  def edgeKVPairs: Iterator[(EdgeId, E)] = _eAttr.iterator
  def vertexAttrs: GenIterable[V] = _vAttr.values
  def srcVerticesMap: GenMap[EdgeId, VertexId] =
    adj.flatMap(row => row._2.map(_._1 -> row._1))
  def dstVerticesMap: GenMap[EdgeId, VertexId] =
    adj.flatMap(row => row._2.map(adjacency => adjacency._1 -> adjacency._2))
  def edgeAttrOf(e: EdgeId): Option[E] =
    _eAttr.get(e)
  def vertexAttrOf(v: VertexId): Option[V] =
    _vAttr.get(v)
  def destinationFrom(o: VertexId, e: EdgeId): Option[VertexId] =
    adj.get(o) match {
      case Some(adjacencies) => adjacencies.get(e)
      case None => None
    }
  def neighborTriplets(v: VertexId): Iterator[Triplet] =
    adj.get(v) match {
      case Some(adjacencies) => adjacencies.map(tup => Triplet(v, tup._1, tup._2)).iterator
      case None => Iterator.empty
    }
  def neighborTripletAttrs(v: VertexId): Iterator[TripletAttrs] =
    neighborTriplets(v)
      .map(triplet => {
        vertexAttrOf(triplet.o) match {
          case Some(o) =>
            edgeAttrOf(triplet.e) match {
              case Some(e) =>
                vertexAttrOf(triplet.d) match {
                  case Some(d) => Some(TripletAttrs(o,e,d))
                  case _ => None
                }
              case _ => None
            }
          case _ => None
        }
      }).flatten
  def incidentEdgeAttrs(v: VertexId): Iterator[E] =
    neighborTriplets(v)
      .map(triplet => edgeAttrOf(triplet.e))
      .flatten

  def updateVertexAttribute(v: VertexId, attr: V): LocalGraph[V, E] =
    new LocalGraph(adj, _vAttr.updated(v, attr), _eAttr)
def updateEdgeAttribute(e: EdgeId, attr: E): LocalGraph[V, E] =
  new LocalGraph(adj, _vAttr, _eAttr.updated(e, attr))

  def addVertex(v: VertexId, attr: V): LocalGraph[V, E] =
    updateVertexAttribute(v, attr)
  def addEdge(t: Triplet, attr: E): LocalGraph[V, E] = {
    if (_vAttr.keySet(t.o) && _vAttr.keySet(t.d)) {
      val previousList: Map[EdgeId, VertexId] = if (adj.isDefinedAt(t.o)) adj(t.o) else Map.empty[EdgeId, VertexId]
      new LocalGraph(
        adj.updated(t.o, previousList.updated(t.e, t.d)),
        _vAttr,
        _eAttr.updated(t.e, attr))
    }
    else this
  }
  // TODO: delete ops need to modify the adjacency matrix
  //  def deleteVertex(v: VertexId): LocalGraph[V, E] =
  //    new LocalGraph(adj, _v - v, _e)
  //  def deleteEdge(e: EdgeId): LocalGraph[V, E] =
  //    new LocalGraph(adj, _v, _e - e)
  def par: LocalGraph[V, E] =
    new LocalGraph(adj.par, _vAttr.par, _eAttr.par)

  override def toString: String =
    adjacencyList.map(kv => s"${kv._1.toString} | ${kv._2.mkString("->")}").mkString("\n")
}

object LocalGraph {
  def apply[A,B](): LocalGraph[A,B] =
    new LocalGraph[A,B](Map.empty[VertexId, Map[EdgeId, VertexId]], Map.empty[VertexId, A], Map.empty[EdgeId, B])
  def apply[A,B](adj: Map[VertexId, Map[EdgeId, VertexId]], v: Map[VertexId, A], e: Map[EdgeId, B]): LocalGraph[A,B] =
    new LocalGraph[A,B](adj, v, e)
}