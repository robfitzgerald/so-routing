package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp

import cse.fitzgero.sorouting.algorithm.pathsearch.KSP
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.{KSPBounds, NoKSPBounds}
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.{LocalGraphODPairByEdge, LocalGraphODPairByVertex, LocalGraphODPath}
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeMATSim, LocalGraphMATSim, VertexMATSim}

import scala.collection.GenSeq

/**
  * Solves Edge-oriented K-Shortest-Paths problems, as MATSim is an edge-oriented simulator
  * Internally determines if KSP needs to be called, and if so, frames the problem as a vertex-oriented KSP
  * and calls the Vertex-Oriented solver.
  */
class LocalGraphMATSimKSP extends KSP[LocalGraphMATSim, LocalGraphODPairByEdge, LocalGraphODPath]{

  val KSP: LocalGraphSimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim] =
    LocalGraphSimpleKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()

  override def kShortestPaths(graph: LocalGraphMATSim, od: LocalGraphODPairByEdge, k: Int = 1, bounds: KSPBounds = NoKSPBounds): GenSeq[LocalGraphODPath] = {
    val oTriplet = graph.edgeTripletOf(od.src).get
    val dTriplet = graph.edgeTripletOf(od.dst).get

    if (oTriplet.e == dTriplet.e) {

      // the source is the destination
      Seq(LocalGraphODPath(od.personId, oTriplet.o, dTriplet.d, List(), List()))

    } else {
      val (oCost, dCost) =
        graph.edgeAttrOf(oTriplet.e) match {
          case Some(oAttr) =>
            graph.edgeAttrOf(dTriplet.e) match {
              case Some(dAttr) =>
                (oAttr.linkCostFlow, dAttr.linkCostFlow)
              case None => (oAttr.linkCostFlow, 0D)
            }
          case None => (0D, 0D)
        }
      if (oTriplet.d == dTriplet.o) {

        // the path is two edges long and is the source and destination edges only
        Seq(LocalGraphODPath(od.personId, oTriplet.o, dTriplet.d, List(oTriplet.e, dTriplet.e), List(oCost, dCost)))

      } else {

        // let's run the Vertex-Oriented SSSP, using the oTriplet destination and dTriplet origin, and then add the first and last edges to the result
        val odByVertex = LocalGraphODPairByVertex(od.personId, oTriplet.d, dTriplet.o)
        val result = KSP.kShortestPaths(graph, odByVertex, k, bounds)
        result.map(res => res.copy(
          path = oTriplet.e +: res.path :+ dTriplet.e,
          cost = oCost +: res.cost :+ dCost
        ))
      }
    }
  }
}

object LocalGraphMATSimKSP {
  def apply(): LocalGraphMATSimKSP = new LocalGraphMATSimKSP
}