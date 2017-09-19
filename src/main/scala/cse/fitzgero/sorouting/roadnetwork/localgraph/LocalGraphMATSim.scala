package cse.fitzgero.sorouting.roadnetwork.localgraph

import scala.collection.{GenMap, GenSeq, GenSet}

class LocalGraphMATSim (
  adj: GenMap[VertexId, Map[EdgeId, VertexId]],
  _vAttr: GenMap[VertexId, VertexMATSim],
  _eAttr: GenMap[EdgeId, EdgeMATSim],
  _eTrip: GenMap[EdgeId, Triplet])
  extends LocalGraph[VertexMATSim, EdgeMATSim](adj, _vAttr, _eAttr) {

  def edgeTripletOf(e: EdgeId): Option[Triplet] =
    _eTrip.get(e)

  def replaceEdgeAttributeList(es: GenSeq[EdgeMATSim]): LocalGraphMATSim = {
    val newEdges: GenMap[EdgeId, EdgeMATSim] =
      es.map(e => e.id -> e).toMap
    new LocalGraphMATSim(adj, _vAttr, newEdges, _eTrip)
  }
  def integrateEdgeAttributeList(es: GenSeq[EdgeMATSim]): LocalGraphMATSim = {
    val esKeys: GenSet[EdgeId] = es.map(_.id).toSet
    val _eNotModified: GenMap[EdgeId, EdgeMATSim] = _eAttr.filter(edge => !esKeys(edge._1))
    new LocalGraphMATSim(
      adj,
      _vAttr,
      _eNotModified ++ es.map(e => e.id -> e),
      _eTrip
    )
  }

  override def updateVertexAttribute(v: VertexId, attr: VertexMATSim): LocalGraphMATSim =
    new LocalGraphMATSim(adj, _vAttr.updated(v, attr), _eAttr, _eTrip)
override def updateEdgeAttribute(e: EdgeId, attr: EdgeMATSim): LocalGraphMATSim =
  new LocalGraphMATSim(adj, _vAttr, _eAttr.updated(e, attr), _eTrip)
  override def addVertex(v: VertexId, attr: VertexMATSim): LocalGraphMATSim =
    updateVertexAttribute(v, attr)
  override def addEdge(t: Triplet, attr: EdgeMATSim): LocalGraphMATSim = {
    if (_vAttr.keySet(t.o) && _vAttr.keySet(t.d)) {
      val previousList: Map[EdgeId, VertexId] = if (adj.isDefinedAt(t.o)) adj(t.o) else Map.empty[EdgeId, VertexId]
      new LocalGraphMATSim(
        adj.updated(t.o, previousList.updated(t.e, t.d)),
        _vAttr,
        _eAttr.updated(t.e, attr),
        _eTrip.updated(t.e, t)
      )
    }
    else this
  }
  // TODO: delete ops need to modify the adjacency matrix
  //  override def deleteVertex(v: VertexId): LocalGraphMATSim =
  //    new LocalGraphMATSim(adj, _v - v, _e)
  //  override def deleteEdge(e: EdgeId): LocalGraphMATSim =
  //    new LocalGraphMATSim(adj, _v, _e - e)
  override def par: LocalGraphMATSim =
    new LocalGraphMATSim(adj.par, _vAttr.par, _eAttr.par, _eTrip.par)
}

object LocalGraphMATSim {
  def apply(): LocalGraphMATSim =
    new LocalGraphMATSim(Map(), Map(), Map(), Map())
  def apply(
    adj: GenMap[VertexId, Map[EdgeId, VertexId]],
    _v: GenMap[VertexId, VertexMATSim],
    _e: GenMap[EdgeId, EdgeMATSim],
    _eTrip: GenMap[EdgeId, Triplet]): LocalGraphMATSim =
    new LocalGraphMATSim(adj, _v, _e, _eTrip)
}

