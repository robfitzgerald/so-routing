package cse.fitzgero.sorouting.algorithm.routing

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.KSPBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPair
import cse.fitzgero.sorouting.algorithm.flowestimation.FWBounds
import cse.fitzgero.sorouting.matsimrunner.population.Population
import cse.fitzgero.sorouting.roadnetwork.RoadNetwork

import scala.concurrent.Future

trait Routing [G <: RoadNetwork, O <: Population] {
  def route(g: G, odPairs: O, config: RoutingConfig): Future[RoutingResult]
}

sealed trait RoutingConfig {
  def k: Int
  def kBounds: KSPBounds
  def fwBounds: FWBounds
}

// TODO: remove blocksize argument, rename numProcs, or generally review parallelization here
case class ParallelRoutingConfig(k: Int = 4, kBounds: KSPBounds, fwBounds: FWBounds, numProcs: Int = Runtime.getRuntime.availableProcessors(), parallelBlockSize: Int = Runtime.getRuntime.availableProcessors()) extends RoutingConfig
case class LocalRoutingConfig(k: Int = 4, kBounds: KSPBounds, fwBounds: FWBounds) extends RoutingConfig