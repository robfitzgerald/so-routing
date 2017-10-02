package cse.fitzgero.sorouting.algorithm.pathselection.localgraph

import java.time.Instant

import scala.collection.{GenMap, GenSeq}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath
import cse.fitzgero.sorouting.algorithm.pathselection.{PathSelection, PathSelectionEmptySet, PathSelectionResult}
import cse.fitzgero.sorouting.roadnetwork.localgraph.{EdgeId, LocalGraphMATSim}

object LocalGraphPathSelection extends PathSelection[LocalGraphODPath, LocalGraphMATSim] {

  /**
    * given a set of alternate paths for each O/D pair, select a best fit, and return the set of best fit paths
    * @param set for each OD Pair, a set of alternate paths
    * @param graph a road network. we will add flows for each possible combination of these sets, and return the set with each driver that has the minimal cost
    */
  override def run(set: GenSeq[GenSeq[LocalGraphODPath]], graph: LocalGraphMATSim): Future[PathSelectionResult] = {

    val startTime = Instant.now.toEpochMilli
    val (allChoiceCombinations, originalsMap): (GenSeq[SelectData], GenMap[Tag, LocalGraphODPath]) = _prepareSet(set)

    Future {
      if (set.isEmpty) PathSelectionEmptySet
      else {
        val findCostOfThisChoiceSet = _findCostOfChoiceSet(originalsMap, graph)_

        val selectedPathSet: GenSeq[LocalGraphODPath] =
          generateChoices(allChoiceCombinations)
            .map(findCostOfThisChoiceSet)
            .minBy(_._2)
            ._1
            .map(originalsMap(_)) // map from Tags to the ODPaths that they pointed at originally

        val endTime = Instant.now.toEpochMilli
        LocalGraphPathSelectionResult(selectedPathSet, endTime - startTime)
      }
    }
  }

  /**
    * Creates two collections, one for finding combinations by personId and Tag (a smaller dataset to find combinations from), and one for selecting ODPaths by those Tags
    * @param set the K shortest paths set
    * @return a tuple containing the two collections
    */
  def _prepareSet(set: GenSeq[GenSeq[LocalGraphODPath]]): (GenSeq[SelectData], GenMap[Tag, LocalGraphODPath]) = {
    def makeTag(personId: String, index: Int): String = s"$personId#$index"
    val (all, orig) =
      set
        .flatMap(
          _
            .zipWithIndex // attach unique index for tagging
            .map(tup => {
            val tag = makeTag(tup._1.personId, tup._2)
            (SelectData(tup._1.personId, tag, tup._1.cost.sum), (tag, tup._1))
          })
        ).unzip
    (all, orig.toMap)
  }

  /**
    * generates all combinations of routes but without duplicates from the same driver
    * @param choices the set of route alternates for all drivers, where choice.personId can be non-unique but choice.tag is unique
    * @param solution a collection of tags where each one is associated with a unique personId
    * @return the possible combinations
    */
  def generateChoices(choices: GenSeq[SelectData], solution: Seq[Tag] = Seq.empty[Tag]): GenSeq[Seq[Tag]] = {
    if (choices.isEmpty) Seq(solution)
    else {
      val filterChoicesByPerson = _constrainBy(choices)_
      choices.filter(_.personId == choices.head.personId).flatMap(choice => generateChoices(filterChoicesByPerson(choice), choice.tag +: solution))
    }
  }

  /**
    * given a set of choices and the current choice, filter the choice set so that there are none with the same personId
    * @param choices the set of all choices
    * @param choice the current choice we are recursing on
    * @return the filtered choice set
    */
  def _constrainBy(choices: GenSeq[SelectData])(choice: SelectData): GenSeq[SelectData] =
    choices.filter(_.personId != choice.personId)

  /**
    * uses the road network graph and the lookup table for the original path data to find the cost of a choiceSet
    * @param originalsMap lookup from Tag to ODPath
    * @param graph road network
    * @param choiceSet a collection of Tags which is the size of the original request pool and has exactly one entry per personId
    * @return the choiceSet along with the cost of adding these flows to the network
    */
  def _findCostOfChoiceSet(originalsMap: GenMap[Tag, LocalGraphODPath], graph: LocalGraphMATSim)(choiceSet: Seq[Tag]): (Seq[Tag], Double) = {
    val edgesAndFlowsToAdd: Map[EdgeId, Int] =
      choiceSet
        .map(originalsMap(_))
        .flatMap(_.path)
        .groupBy(identity)
        .map(tup => (tup._1, tup._2.size))

    val updatedGraphSystemCost: Double =
      edgesAndFlowsToAdd.foldLeft(graph)((g, edgeInfo) => {
        val oldEdgeValue = g.edgeAttrOf(edgeInfo._1).get
        g.updateEdgeAttribute(edgeInfo._1, oldEdgeValue.copy(flowUpdate = edgeInfo._2))
      }).edgeAttrs
        .map(_.linkCostFlow)
        .sum

    // Output this choiceSet and the total cost it has
    (choiceSet, updatedGraphSystemCost)
  }

  override type Result = LocalGraphPathSelectionResult
  type Tag = String

  case class SelectData(personId: String, tag: String, cost: Double)
}