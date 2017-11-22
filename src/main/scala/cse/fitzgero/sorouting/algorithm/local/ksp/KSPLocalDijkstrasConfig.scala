package cse.fitzgero.sorouting.algorithm.local.ksp

import cse.fitzgero.graph.config.KSPBounds

/**
  * configures this KSP algorithm
 *
  * @param k the number of desired alternate paths
  * @param kspBounds an upper bounds on the search for valid alternate paths
  * @param overlapThreshold the percentage that alternate paths can overlap, in the range [0.0%, 100.0%]
  */
case class KSPLocalDijkstrasConfig(k: Int = 1, kspBounds: Option[KSPBounds] = None, overlapThreshold: Double = 1.0D) {
  require(k > 0)
  require(overlapThreshold <= 1.0D)
  require(overlapThreshold >= 0.0D)
}
