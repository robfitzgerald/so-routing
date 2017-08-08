package cse.fitzgero.sorouting.algorithm.routing

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath

sealed trait RoutingResult {
  type ODPathSubclass <: ODPath[_,_]
}

abstract class RoutingSolution extends RoutingResult {
  def routes: Seq[ODPathSubclass]
}

/**
  * for cases when routing fails.  why would it fail?
  * - we could have a timeout
  * -
  */
case class NoRoutingSolution (e: RoutingFailure) extends RoutingResult


/**
  * an algebra of failure types to match against when receiving a NoRoutingSolution result
  */
sealed trait RoutingFailure
case class Timeout(t: Long, limit: Long) extends RoutingFailure
case object UnknownRoutingFailure extends RoutingFailure
