package cse.fitzgero.sorouting.algorithm.routing.localgraphrouting

import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.algorithm.routing._

case class LocalGraphRoutingResult (routes: Seq[LocalGraphODPath]) extends RoutingSolution
