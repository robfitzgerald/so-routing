package cse.fitzgero.sorouting.algorithm.routing

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.KSPBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPair
import cse.fitzgero.sorouting.algorithm.trafficassignment.TerminationCriteria
import cse.fitzgero.sorouting.roadnetwork.RoadNetwork

import scala.concurrent.Future

trait Routing [G <: RoadNetwork, O <: ODPair[_]] {
  def route(g: G, odPairs: Seq[O], config: RoutingConfig): Future[RoutingResult]
}

sealed trait RoutingConfig {
  def k: Int
  def kBounds: KSPBounds
  def fwBounds: TerminationCriteria
}

case class ParallelRoutingConfig(k: Int = 4, kBounds: KSPBounds, fwBounds: TerminationCriteria, numProcs: Int = 8, parallelBlockSize: Int = 8) extends RoutingConfig
case class LocalRoutingConfig(k: Int = 4, kBounds: KSPBounds, fwBounds: TerminationCriteria) extends RoutingConfig