package cse.fitzgero.sorouting.algorithm.pathselection.localgraph

import cse.fitzgero.sorouting.SORoutingAsyncUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph._

import scala.collection.GenSeq
import scala.collection.parallel.ParSeq

class LocalGraphPathSelectionTests extends SORoutingAsyncUnitTestTemplate {
  "LocalGraphPathSelection" when {

    val networkFilePath: String =       "src/test/resources/LocalGraphPathSelectionTests/network-matsim-example-equil.xml"
    val snapshotFilePath: String =      "src/test/resources/LocalGraphPathSelectionTests/snapshot-matsim-example-equil.xml"

    "called with a set of possible alternate paths for a set of OD pairs" should {
      "find the optimal set" in {

        val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get

        val requests: GenSeq[GenSeq[LocalGraphODPath]] = ParSeq(
          Seq(
            LocalGraphODPath("1", 1, 15, List[EdgeId]("1","2","11","20","21","22"), List[Double](1,2,0,0,0,0)),
            LocalGraphODPath("1", 1, 15, List[EdgeId]("1","3","12","20","21","22"), List[Double](1,4,0,0,0,0)),
            LocalGraphODPath("1", 1, 15, List[EdgeId]("1","4","13","20","21","22"), List[Double](1,8,0,0,0,0))
          ),
          Seq(
            LocalGraphODPath("2", 1, 15, List[EdgeId]("1","2","11","20","21","22"), List[Double](1,2,0,0,0,0)),
            LocalGraphODPath("2", 1, 15, List[EdgeId]("1","6","15","20","21","22"), List[Double](1,18,0,0,0,0)),
            LocalGraphODPath("2", 1, 15, List[EdgeId]("1","5","14","20","21","22"), List[Double](1,10,0,0,0,0))
          )
        )

        val PathSelection = LocalGraphPathSelection()
        PathSelection.run(requests, graph) map {
          case LocalGraphPathSelectionResult(paths, runTime) =>
            println(paths)
            println(runTime)
            paths.size should equal (2)
            paths.filter(_.personId == "2").head.path should equal(List[EdgeId]("1","2","11","20","21","22"))
          case _ => fail()
        }
      }
    }
  }
}
