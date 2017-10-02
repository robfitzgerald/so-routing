package cse.fitzgero.sorouting.algorithm.pathselection.localgraph

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.algorithm.pathselection.localgraph.LocalGraphPathSelection.{SelectData, Tag}
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{LocalGraphMATSim, LocalGraphMATSimFactory}

import scala.collection.{GenMap, GenSeq}

class LocalGraphPathSelectionSyncTests extends SORoutingUnitTestTemplate {
  "LocalGraphPathSelection" when {
    "_prepareSet" when {
      "called with a set of od paths" should {
        "return the tuple of prepared data, where each data point has a unique tag corresponding to it's original data" in {
          val result: (GenSeq[SelectData], GenMap[Tag, LocalGraphODPath]) =
            LocalGraphPathSelection._prepareSet(TestAssets.requests)

          result._1.map(_.tag).distinct.size should equal (TestAssets.requests.flatten.size)
          result._1.map(_.tag).forall(tag => result._2.isDefinedAt(tag)) should be (true)
          result._2.keys.size should equal (TestAssets.requests.flatten.size)
          result._1.foreach(selectData => {
            result._2(selectData.tag).personId should equal (selectData.personId)
          })
        }
      }
    }
  }
  "_findCostOfChoiceSet" when {
    "passed a set of original paths, their lookup tags, and a road network" should {
      "assign the resulting network cost of each tag sequence" in {
        val graph: LocalGraphMATSim =
          LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10)
            .fromFileAndSnapshot(TestAssets.networkFilePath, TestAssets.snapshotFilePath).get
        val (choices, originals) = LocalGraphPathSelection._prepareSet(TestAssets.requests)
        val choiceSets = LocalGraphPathSelection.generateChoices(choices)
        val findCost = LocalGraphPathSelection._findCostOfChoiceSet(originals, graph)_
        val result = choiceSets.map(findCost(_))
//        println(result)
        result.map(_._1.toSet).distinct.size should equal (TestAssets.requests.map(_.size).product)
      }
    }
  }
  "_constrainBy" when {
    "called with all remaining choices and the current choice" should {
      "remove instances of the person id associated with this choice" in {
        val (selectData, originalsMap): (GenSeq[SelectData], GenMap[Tag, LocalGraphODPath]) =
          LocalGraphPathSelection._prepareSet(TestAssets.requests)
        val ourChoice = selectData.head
        val result = LocalGraphPathSelection._constrainBy(selectData)(ourChoice)
        result.forall(_.personId != ourChoice.personId) should equal (true)
      }
    }
  }
  "generateChoices" when {
    "passed the output of _prepareSet on a set of requests" should {
      "create all combinations of sets where there is a single route option per personId" in {
        val (choices, originals) = LocalGraphPathSelection._prepareSet(TestAssets.requests)
        val result = LocalGraphPathSelection.generateChoices(choices)
        result.foreach(println)
        result.map(_.toSet).distinct.size should equal (TestAssets.requests.map(_.size).product)
      }
    }
  }
}
