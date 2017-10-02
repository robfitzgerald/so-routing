package cse.fitzgero.sorouting.algorithm.pathselection.localgraph

import cse.fitzgero.sorouting.SORoutingAsyncUnitTestTemplate
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph._


class LocalGraphPathSelectionAsyncTests extends SORoutingAsyncUnitTestTemplate {
  "LocalGraphPathSelection" when {
    "run" when {
      "called with a set of possible alternate paths for a set of OD pairs" should {
        "find the optimal set" in {

          val graph: LocalGraphMATSim =
            LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10)
              .fromFileAndSnapshot(TestAssets.networkFilePath, TestAssets.snapshotFilePath).get

          LocalGraphPathSelection.run(TestAssets.requests, graph) map {
            case LocalGraphPathSelectionResult(paths, runTime) =>
              // there should be one result per person: 2 total
              paths.size should equal (2)
              // person 2's alternates were very poor. it should force them to select the path through -[2]->-[11]->
              paths.filter(_.personId == "2").head.path should equal(List[EdgeId]("1","2","11","20","21","22"))
              // because person 2 was forced to -[2]->-[11]->, it means person 1 should be pushed to -[3]->-[12]->
              // since the combined congestion effect of sending two vehicles on -[2]->-[11]-> is greater
              paths.filter(_.personId == "1").head.path should equal(List[EdgeId]("1","3","12","20","21","22"))
            case _ => fail()
          }
        }
      }
    }
  }
}
