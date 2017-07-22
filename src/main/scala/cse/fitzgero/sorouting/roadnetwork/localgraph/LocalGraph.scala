package cse.fitzgero.sorouting.roadnetwork.localgraph

class LocalGraph [V, E] private[localgraph]
  (adj: Map[VertexId, Map[EdgeId, VertexId]], _v: Map[VertexId, V], _e: Map[EdgeId, E]) {

  case class Triplet(o: VertexId, e: EdgeId, d: VertexId)
  case class TripletAttrs(o: V, e: E, d: V)

  def adjacencyList: Map[VertexId, Map[EdgeId, VertexId]] = adj
  def vertices: Iterable[VertexId] = _v.keys
  def edges: Iterable[EdgeId] = _e.keys
  def edgeAttrs: Iterable[E] = _e.values
  def vertexAttrs: Iterable[V] = _v.values
  def edgeAttrOf(e: EdgeId): Option[E] =
    if (_e.isDefinedAt(e)) Some(_e(e))
    else None
  def vertexAttrOf(v: VertexId): Option[V] =
    if (_v.isDefinedAt(v)) Some(_v(v))
    else None
  def neighborTriplets(v: VertexId): Iterator[Triplet] =
    if (adj.isDefinedAt(v)) adj(v).map(tup => Triplet(v, tup._1, tup._2)).iterator
    else Iterator.empty
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
  def updateVertex(v: VertexId, attr: V): LocalGraph[V, E] =
    new LocalGraph(adj, _v.updated(v, attr), _e)
  def deleteVertex(v: VertexId): LocalGraph[V, E] =
    new LocalGraph(adj, _v - v, _e)
  def updateEdge(e: EdgeId, attr: E): LocalGraph[V, E] =
    new LocalGraph(adj, _v, _e.updated(e, attr))
  def deleteEdge(e: EdgeId): LocalGraph[V, E] =
    new LocalGraph(adj, _v, _e - e)
  def addVertex(v: VertexId, attr: V): LocalGraph[V, E] =
    updateVertex(v, attr)
  def addEdge(t: Triplet, attr: E): LocalGraph[V, E] = {
    if (_v.keySet(t.o) && _v.keySet(t.d)) {
      val previousList: Map[EdgeId, VertexId] = if (adj.isDefinedAt(t.o)) adj(t.o) else Map.empty[EdgeId, VertexId]
      new LocalGraph(
        adj.updated(t.o, previousList.updated(t.e, t.d)),
        _v,
        _e.updated(t.e, attr))
    }
    else this
  }

  override def toString: String =
    adjacencyList.map(kv => s"${kv._1.toString} | ${kv._2.mkString("->")}").mkString(" ")
}

object LocalGraph {
  def apply[A,B](): LocalGraph[A,B] =
    new LocalGraph[A,B](Map.empty[VertexId, Map[EdgeId, VertexId]], Map.empty[VertexId, A], Map.empty[EdgeId, B])
}