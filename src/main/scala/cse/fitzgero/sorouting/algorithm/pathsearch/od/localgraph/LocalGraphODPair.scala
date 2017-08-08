package cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPair
import cse.fitzgero.sorouting.roadnetwork.localgraph.VertexId

case class LocalGraphODPair(srcVertex: VertexId, dstVertex: VertexId) extends ODPair[VertexId]
