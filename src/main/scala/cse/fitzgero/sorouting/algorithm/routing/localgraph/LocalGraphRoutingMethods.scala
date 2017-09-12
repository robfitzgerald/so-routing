package cse.fitzgero.sorouting.algorithm.routing.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.{KSPLocalGraphMATSimResult, LocalGraphMATSimKSP}
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.{LocalGraphODPairByEdge, LocalGraphODPairByVertex}
import cse.fitzgero.sorouting.algorithm.routing.{LocalRoutingConfig, ParallelRoutingConfig, RoutingConfig}
import cse.fitzgero.sorouting.algorithm.trafficassignment.TrafficAssignmentResult
import cse.fitzgero.sorouting.algorithm.trafficassignment.localgraph.LocalGraphFrankWolfe
import cse.fitzgero.sorouting.roadnetwork.localgraph.LocalGraphMATSim

import scala.collection.GenSeq
import scala.concurrent.Future

object LocalGraphRoutingMethods {
  //  selecting our routes starts with a KSP tree for each od pair. we want to select exactly one route for that od.
  //
  //  map the collection of KSP trees to a recursive function that will
  //   find the best child based on a lookup of the edge in the graph
  //   add that edge to the head of a list and recurse by passing that child

  val KSP: LocalGraphMATSimKSP = LocalGraphMATSimKSP()


  def findKShortest(g: LocalGraphMATSim, odPairs: Seq[LocalGraphODPairByEdge], config: RoutingConfig): Future[GenSeq[KSPLocalGraphMATSimResult]] = {
    config match {
      case ParallelRoutingConfig(k, kspBounds, _, procs, blockSize) =>
        // TODO: use procs value (modify ExecutionContext?)
        Future {
          odPairs.grouped(blockSize).flatMap(_.par.map(od => KSP.kShortestPaths(g, od, k, kspBounds))).toSeq
        }
      case LocalRoutingConfig(k, kSPBounds, _) =>
        Future {
          odPairs.map(od => KSP.kShortestPaths(g, od, k, kSPBounds))
        }
    }
  }


  def trafficAssignmentOracleFlow(g: LocalGraphMATSim, odPairs: GenSeq[LocalGraphODPairByVertex], config: RoutingConfig): Future[TrafficAssignmentResult] =
    config match {
      case ParallelRoutingConfig(k, kspBounds, _, procs, blockSize) =>
        // TODO: use procs value (modify ExecutionContext?)
        Future {
          LocalGraphFrankWolfe.solve(g.par, odPairs.par, config.fwBounds)
        }
      case LocalRoutingConfig(k, kSPBounds, _) =>
        Future {
          LocalGraphFrankWolfe.solve(g, odPairs, config.fwBounds)
        }
    }

}
