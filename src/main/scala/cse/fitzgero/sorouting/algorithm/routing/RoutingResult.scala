package cse.fitzgero.sorouting.algorithm.routing

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath

sealed trait RoutingResult {
}

abstract class RoutingSolution [O <: ODPath[_,_]] extends RoutingResult {
  def routes: Seq[O]
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
