package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp

import java.time.Instant

import com.typesafe.config.ConfigFactory
import cse.fitzgero.sorouting.algorithm.pathsearch.KSP
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.{KSPBounds, NoKSPBounds}
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.{LocalGraphODPairByEdge, LocalGraphODPairByVertex, LocalGraphODPath}
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeMATSim, LocalGraphMATSim, VertexMATSim}
import cse.fitzgero.sorouting.util.Logging

/**
  * Solves Edge-oriented K-Shortest-Paths problems, as MATSim is an edge-oriented simulator
  * Internally determines if KSP needs to be called, and if so, frames the problem as a vertex-oriented KSP
  * and calls the Vertex-Oriented solver.
  */
class LocalGraphMATSimKSP extends KSP[LocalGraphMATSim, LocalGraphODPairByEdge, LocalGraphODPath] with Logging {

  val KSP: LocalGraphKSP[LocalGraphMATSim, VertexMATSim, EdgeMATSim] =
    ConfigFactory.load().getInt("soRouting.algorithm.ksp.localgraph.version") match {
      case 1 => LocalGraphSimpleKSP01[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
      case 2 => LocalGraphSimpleKSP02[LocalGraphMATSim, VertexMATSim, EdgeMATSim]()
    }
  val startTime: Long = Instant.now().toEpochMilli

  override def kShortestPaths(graph: LocalGraphMATSim, od: LocalGraphODPairByEdge, k: Int = 1, bounds: KSPBounds = NoKSPBounds): KSPLocalGraphMATSimResult = {
    val oTriplet = graph.edgeTripletOf(od.src).get
    val dTriplet = graph.edgeTripletOf(od.dst).get

    if (oTriplet.e == dTriplet.e) {

      // the source is the destination
      KSPLocalGraphMATSimResult(
        Seq(LocalGraphODPath(od.personId, oTriplet.o, dTriplet.d, List(), List())),
        k,
        0,
        Instant.now().toEpochMilli - startTime
      )

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
        KSPLocalGraphMATSimResult(
          Seq(LocalGraphODPath(od.personId, oTriplet.o, dTriplet.d, List(oTriplet.e, dTriplet.e), List(oCost, dCost))),
          k,
          0,
          Instant.now().toEpochMilli - startTime
        )

      } else {

        // let's run the Vertex-Oriented SSSP, using the oTriplet destination and dTriplet origin, and then add the first and last edges to the result
        val odByVertex = LocalGraphODPairByVertex(od.personId, oTriplet.d, dTriplet.o)
        val result = KSP.kShortestPaths(graph, odByVertex, k, bounds)
        val kspMATSimPaths = result.paths.map(res => res.copy(
          path = oTriplet.e +: res.path :+ dTriplet.e,
          cost = oCost +: res.cost :+ dCost
        ))
        logger.info(f"[ksp] ${od.src} -> ${od.dst} has ${result.kSelected} paths with ${result.paths.flatMap(_.path).distinct.size.toDouble / result.paths.flatMap(_.path).size}%02f %% unique edges in its edge set")
        KSPLocalGraphMATSimResult(
          kspMATSimPaths,
          result.kRequested,
          result.kSelected,
          Instant.now().toEpochMilli - startTime
        )
      }
    }
  }
}

object LocalGraphMATSimKSP {
  def apply(): LocalGraphMATSimKSP = new LocalGraphMATSimKSP
}