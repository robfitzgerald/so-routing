package cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp

import cse.fitzgero.sorouting.algorithm.pathsearch.KSP
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.{KSPBounds, KSPResult}
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.{LocalGraphODPairByVertex, LocalGraphODPath}
import cse.fitzgero.sorouting.roadnetwork.edge.EdgeProperty
import cse.fitzgero.sorouting.roadnetwork.localgraph.LocalGraph
import cse.fitzgero.sorouting.roadnetwork.vertex.VertexProperty

abstract class LocalGraphKSP[G <: LocalGraph[V,E], V <: VertexProperty[_], E <: EdgeProperty] extends KSP [G, LocalGraphODPairByVertex, LocalGraphODPath] {
  def kShortestPaths (graph: G, od: LocalGraphODPairByVertex, k: Int, bounds: KSPBounds): KSPLocalGraphResult
}