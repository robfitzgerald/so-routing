package cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeId, VertexId}

case class LocalGraphODPath(srcVertex: VertexId, dstVertex: VertexId, path: List[EdgeId], cost: List[Double]) extends ODPath[VertexId, EdgeId]