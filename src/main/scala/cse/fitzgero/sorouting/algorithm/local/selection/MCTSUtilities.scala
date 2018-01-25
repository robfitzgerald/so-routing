package cse.fitzgero.sorouting.algorithm.local.selection

import scala.collection.GenMap
import scala.util.Random

import cse.fitzgero.sorouting.algorithm.local.selection.SelectionLocalMCTSAlgorithm.{MCTSAltPath, MCTSTreeNode, PersonID, Tag}

/**
  * Provides pseudorandom support functions for MCTS
  * @param randomSeed an optional random seed value, used for testing
  * @param CongestionRatioThreshold the percent that congestion is allowed to grow within a state where it is still given a reward
  * @param globalAlts the list of all possible alternate paths for each person
  */
class MCTSUtilities(randomSeed: Option[Int], CongestionRatioThreshold: Double, globalAlts: GenMap[PersonID, GenMap[Tag, Seq[String]]]) {

  val random: Random = randomSeed match {
    case Some(seed) => new Random(seed)
    case None => new Random
  }

  /**
    * gives a reward value only if none of the costs increase by CongestionRatioThreshold
    * fails to
    * @param costs the edges paired with their starting costs and the costs from this group
    * @return 1 or 0
    */
  def forAllCostDiff(costs: List[(String, Double, Double)]): Int = {
    val testResult = costs.forall {
      cost =>
        if (cost._2 == 0) true // @TODO: this would fail us when a flow transitions from 0 to something above the congestion threshold!
        else (cost._3 / cost._2) <= CongestionRatioThreshold
    }
    if (testResult) 1 else 0
  }

  /**
    * gives a reward value if the average of the costs do not exceed CongestionRatioThreshold
    * @param costs the edges paired with their starting costs and the costs from this group
    * @return 1 or 0
    */
  def meanCostDiff(costs: List[(String, Double, Double)]): Int = {
    if (costs.isEmpty) {
      1 // TODO: costs should never be empty since this algorithm returns None on an empty request.
    } else {
      val avgCostDiff: Double = costs.map {
        tuple =>
          if (tuple._2 == 0) 0.0D
          else tuple._3 / tuple._2
      }.sum / costs.size
      val testResult: Boolean = avgCostDiff <= CongestionRatioThreshold
      if (testResult) 1 else 0
    }
  }

  /**
    * backtrack from a tag to the associated (Tag, EdgeList)
    * @param tag a tag (shorthand for a person and a number representing an alternate path)
    * @return the tag along with the edge list related to this tag
    */
  private def getAltPathFrom(tag: Tag): MCTSAltPath = {
    val edges = globalAlts(tag.personId)(tag)
    MCTSAltPath(tag, edges)
  }

  /**
    * a helper that selects a random child in the Expand step
    * @param children the set of children of a given MCTS node
    * @return one child selected by a random process
    */
  def selectionMethod(children: GenMap[Tag, () => Option[MCTSTreeNode]]): MCTSAltPath = {
    val remainingAlts =
      children
        .filter(_._2().isEmpty)
        .keys.map(getAltPathFrom)
        .toVector
    remainingAlts(random.nextInt(remainingAlts.size))
  }
}

object MCTSUtilities {
  def apply(random: Option[Int], CongestionRatioThreshold: Double, globalAlts: GenMap[PersonID, GenMap[Tag, Seq[String]]]): MCTSUtilities =
    new MCTSUtilities(random, CongestionRatioThreshold, globalAlts)
}