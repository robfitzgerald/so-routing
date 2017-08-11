package cse.fitzgero.sorouting.algorithm.routing.localgraphrouting

import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.algorithm.routing._

import scala.collection.GenSeq

case class LocalGraphRoutingResult (routes: GenSeq[LocalGraphODPath] = Seq.empty[LocalGraphODPath], runTime: Long) extends RoutingSolution[LocalGraphODPath]
