//package cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp
//
//import cse.fitzgero.sorouting.algorithm.pathsearch.SSSP
//import cse.fitzgero.sorouting.roadnetwork.edge.EdgeProperty
//import cse.fitzgero.sorouting.roadnetwork.localgraph.LocalGraph
//import cse.fitzgero.sorouting.roadnetwork.vertex.VertexProperty
//
//sealed trait LocalGraphSSSPOrientationSelector {
//  type S <: SSSP[_,_,_]
//  def sssp: S
//}
//
//case class SSSPByVertex [G <: LocalGraph[V,E], V <: VertexProperty[_], E <: EdgeProperty] () extends LocalGraphSSSPOrientationSelector {
//  override def sssp: LocalGraphVertexOrientedSSSP[G, V, E] = LocalGraphVertexOrientedSSSP[G, V, E]()
//}
//
//case class SSSPByEdge() extends LocalGraphSSSPOrientationSelector {
//  override def sssp: LocalGraphMATSimSSSP = LocalGraphMATSimSSSP()
//}