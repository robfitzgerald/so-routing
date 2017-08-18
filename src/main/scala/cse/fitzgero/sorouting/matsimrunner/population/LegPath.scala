package cse.fitzgero.sorouting.matsimrunner.population

import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType

sealed trait LegPath
case object NoRouteAssigned extends LegPath
case class RouteAssigned(path: Seq[EdgeIdType]) extends LegPath
