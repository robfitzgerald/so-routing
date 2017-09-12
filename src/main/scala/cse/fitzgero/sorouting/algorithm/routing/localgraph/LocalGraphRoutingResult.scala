package cse.fitzgero.sorouting.algorithm.routing.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.algorithm.routing._

import scala.collection.GenSeq

case class LocalGraphRoutingResult (
  routes: GenSeq[LocalGraphODPath] = Seq.empty[LocalGraphODPath],
  kspRunTime: Long,
  fwRunTime: Long,
  routeSelectionRunTime: Long,
  overallRunTime: Long
) extends RoutingSolution[LocalGraphODPath]
