package cse.fitzgero.sorouting.algorithm.pathselection.localgraph

import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.localgraphsimpleksp.KSPLocalGraphResult
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.roadnetwork.localgraph.EdgeId

import scala.collection.GenSeq
import scala.collection.parallel.ParSeq

object TestAssets {
  val networkFilePath: String =       "src/test/resources/LocalGraphPathSelectionTests/network-matsim-example-equil.xml"
  val snapshotFilePath: String =      "src/test/resources/LocalGraphPathSelectionTests/snapshot-matsim-example-equil.xml"

  val requests: GenSeq[KSPLocalGraphResult] = ParSeq(
    KSPLocalGraphResult(
      paths = GenSeq(
        LocalGraphODPath("1", 1, 15, List[EdgeId]("1","2","11","20","21","22"), List[Double](1,2,0,0,0,0)),
        LocalGraphODPath("1", 1, 15, List[EdgeId]("1","3","12","20","21","22"), List[Double](1,4,0,0,0,0)),
        LocalGraphODPath("1", 1, 15, List[EdgeId]("1","4","13","20","21","22"), List[Double](1,8,0,0,0,0))
      ),
      kRequested = 3,
      nExplored = 3,
      runTime = 0L
    ),
    KSPLocalGraphResult(
      paths = GenSeq(
        LocalGraphODPath("2", 1, 15, List[EdgeId]("1","2","11","20","21","22"), List[Double](1,2,0,0,0,0)),
        LocalGraphODPath("2", 1, 15, List[EdgeId]("1","6","15","20","21","22"), List[Double](1,18,0,0,0,0)),
        LocalGraphODPath("2", 1, 15, List[EdgeId]("1","5","14","20","21","22"), List[Double](1,10,0,0,0,0))
      ),
      kRequested = 3,
      nExplored = 3,
      runTime = 0L
    )
  )
}
