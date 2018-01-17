package cse.fitzgero.sorouting.model.population

import java.time.LocalTime

import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

/**
  * routing request for use with a LocalGraph model
  * @param id the unique request id
  * @param od the origin & destination
  * @param requestTime the time we record/observe the request entered into the system as input
  */
case class LocalRequest (id: String, od: LocalODPair, requestTime: LocalTime) extends SORoutingRequest {
  override type OD = LocalODPair
}
