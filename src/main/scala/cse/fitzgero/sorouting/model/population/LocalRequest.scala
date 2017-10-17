package cse.fitzgero.sorouting.model.population

import java.time.LocalTime

import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

case class LocalRequest (id: String, od: LocalODPair, requestTime: LocalTime) extends SORoutingRequest {
  override type OD = LocalODPair
}
