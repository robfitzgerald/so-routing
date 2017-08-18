package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.roadnetwork.localgraph._

sealed trait LegPath
case object NoRouteAssigned extends LegPath
case class RouteAssigned(path: Seq[EdgeId]) extends LegPath
