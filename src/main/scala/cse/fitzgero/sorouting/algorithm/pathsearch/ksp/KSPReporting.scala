package cse.fitzgero.sorouting.algorithm.pathsearch.ksp

import scala.collection.GenSeq
import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath
import cse.fitzgero.sorouting.util.AuxLogger

trait KSPReporting {

  private val logger = AuxLogger.get("algorithm")

  def routesWithKAlts(kspResult: GenSeq[KSPResult], k: Int): Double = {
    val (numRouted, hasKRoutes) = kspResult match {
      case x: GenSeq[KSPSolution[_]] => (x.size, x.count(_.paths.size == k))
      case _ => (0, 0)
    }
    val result: Double = hasKRoutes.toDouble / numRouted.toDouble
    logger.info(this, "routes with k alternates", result)
    result
  }
}
