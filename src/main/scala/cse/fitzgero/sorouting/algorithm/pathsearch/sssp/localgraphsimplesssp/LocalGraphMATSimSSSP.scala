package cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraphsimplesssp

import cse.fitzgero.sorouting.algorithm.pathsearch.SSSP
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.{LocalGraphODPairByEdge, LocalGraphODPairByVertex, LocalGraphODPath}
import cse.fitzgero.sorouting.roadnetwork.localgraph._
import cse.fitzgero.sorouting.util.ClassLogging

class LocalGraphMATSimSSSP extends SSSP[LocalGraphMATSim, LocalGraphODPairByEdge, LocalGraphODPath] with ClassLogging {

  val SSSP: LocalGraphVertexOrientedSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim] =
    LocalGraphVertexOrientedSSSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()

  sealed trait SPTriplets
  case class TripletPair(src: Triplet, dst: Triplet) extends SPTriplets
  case object InvalidPair extends SPTriplets

  /**
    * performs an edge-oriented shortest path search built on Dijkstra's algorithm
    * @param graph a road network
    * @param od an origin/destination pair
    * @return the resulting shortest path
    */
  override def shortestPath(graph: LocalGraphMATSim, od: LocalGraphODPairByEdge): LocalGraphODPath = {

    // TODO: currently unsafe, but that should be ok at this depth, right? The data structure shouldn't be getting confirmed here in the middle of this algorithm.
    val oTriplet = graph.edgeTripletOf(od.src).get
    val dTriplet = graph.edgeTripletOf(od.dst).get

    if (oTriplet.e == dTriplet.e) {

      // the source is the destination
      LocalGraphODPath(od.personId, oTriplet.o, dTriplet.d, List(), List())

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
        LocalGraphODPath(od.personId, oTriplet.o, dTriplet.d, List(oTriplet.e, dTriplet.e), List(oCost, dCost))

      } else {

        // let's run the Vertex-Oriented SSSP, using the oTriplet destination and dTriplet origin, and then add the first and last edges to the result
        val odByVertex = LocalGraphODPairByVertex(od.personId, oTriplet.d, dTriplet.o)
        val result = SSSP.shortestPath(graph, odByVertex)
        result.copy(
          path = oTriplet.e +: result.path :+ dTriplet.e,
          cost = oCost +: result.cost :+ dCost
        )

      }
    }
  }
}

object LocalGraphMATSimSSSP {
  def apply(): LocalGraphMATSimSSSP = new LocalGraphMATSimSSSP
}