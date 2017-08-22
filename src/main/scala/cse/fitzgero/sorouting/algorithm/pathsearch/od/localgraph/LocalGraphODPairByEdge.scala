package cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPair
import cse.fitzgero.sorouting.roadnetwork.localgraph._

case class LocalGraphODPairByEdge(personId: String, src: EdgeId, dst: EdgeId) extends ODPair[EdgeId]