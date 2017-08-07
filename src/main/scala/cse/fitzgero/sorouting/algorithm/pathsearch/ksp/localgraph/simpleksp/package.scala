package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.sssp.localgraph.simplesssp.SimpleSSSP_ODPath
import cse.fitzgero.sorouting.algorithm.pathsearch.{ODPair, ODPath}
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeId, VertexId}

package object simpleksp {

  case class SimpleKSP_ODPair(srcVertex: VertexId, dstVertex: VertexId) extends ODPair[VertexId]

  case class SimpleKSP_ODPath(srcVertex: VertexId, dstVertex: VertexId, path: List[EdgeId], cost: List[Double]) extends ODPath[VertexId, EdgeId]
  object SimpleKSP_ODPath {
    def from(a: SimpleSSSP_ODPath): SimpleKSP_ODPath =
      SimpleKSP_ODPath(a.srcVertex, a.dstVertex, a.path, a.cost)
  }
}
