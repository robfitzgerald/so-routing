package cse.fitzgero.sorouting.algorithm.routing.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.algorithm.routing._
import cse.fitzgero.sorouting.util.LogsGroup

import scala.collection.GenSeq

case class LocalGraphRoutingResult02 (
  routes: GenSeq[LocalGraphODPath] = Seq.empty[LocalGraphODPath],
  logs: LogsGroup = LogsGroup()
) extends RoutingSolution[LocalGraphODPath]
