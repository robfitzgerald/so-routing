package cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPair
import cse.fitzgero.sorouting.roadnetwork.localgraph.VertexId

case class LocalGraphODPairByVertex(personId: String, src: VertexId, dst: VertexId) extends ODPair[VertexId]

