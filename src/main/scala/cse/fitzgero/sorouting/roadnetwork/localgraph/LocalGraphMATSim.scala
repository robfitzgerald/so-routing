package cse.fitzgero.sorouting.roadnetwork.localgraph

import scala.collection.{GenMap, GenSeq, GenSet}

class LocalGraphMATSim
(adj: GenMap[VertexId, Map[EdgeId, VertexId]], _v: GenMap[VertexId, VertexMATSim], _e: GenMap[EdgeId, EdgeMATSim])
  extends LocalGraph[VertexMATSim, EdgeMATSim](adj, _v, _e) {

  def replaceEdgeList(es: GenSeq[EdgeMATSim]): LocalGraphMATSim = {
    val newEdges: GenMap[EdgeId, EdgeMATSim] =
      es.map(e => e.id -> e).toMap
    new LocalGraphMATSim(adj, _v, newEdges)
  }
  def integrateEdgeList(es: GenSeq[EdgeMATSim]): LocalGraphMATSim = {
    val esKeys: GenSet[EdgeId] = es.map(_.id).toSet
    val _eNotModified: GenMap[EdgeId, EdgeMATSim] = _e.filter(edge => !esKeys(edge._1))
    new LocalGraphMATSim(
      adj,
      _v,
      _eNotModified ++ es.map(e => e.id -> e)
    )
  }

  override def updateVertex(v: VertexId, attr: VertexMATSim): LocalGraphMATSim =
    new LocalGraphMATSim(adj, _v.updated(v, attr), _e)
  override def deleteVertex(v: VertexId): LocalGraphMATSim =
    new LocalGraphMATSim(adj, _v - v, _e)
  override def updateEdge(e: EdgeId, attr: EdgeMATSim): LocalGraphMATSim =
    new LocalGraphMATSim(adj, _v, _e.updated(e, attr))
  override def deleteEdge(e: EdgeId): LocalGraphMATSim =
    new LocalGraphMATSim(adj, _v, _e - e)
  override def addVertex(v: VertexId, attr: VertexMATSim): LocalGraphMATSim =
    updateVertex(v, attr)
  override def addEdge(t: Triplet, attr: EdgeMATSim): LocalGraphMATSim = {
    if (_v.keySet(t.o) && _v.keySet(t.d)) {
      val previousList: Map[EdgeId, VertexId] = if (adj.isDefinedAt(t.o)) adj(t.o) else Map.empty[EdgeId, VertexId]
      new LocalGraphMATSim(
        adj.updated(t.o, previousList.updated(t.e, t.d)),
        _v,
        _e.updated(t.e, attr))
    }
    else this
  }
  override def parallelize: LocalGraphMATSim =
    new LocalGraphMATSim(adj.par, _v.par, _e.par)
}

object LocalGraphMATSim {
  def apply(): LocalGraphMATSim =
    new LocalGraphMATSim(Map(), Map(), Map())
  def apply(
    adj: GenMap[VertexId, Map[EdgeId, VertexId]],
    _v: GenMap[VertexId, VertexMATSim],
    _e: GenMap[EdgeId, EdgeMATSim]): LocalGraphMATSim =
    new LocalGraphMATSim(adj, _v, _e)
}
