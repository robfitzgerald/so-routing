package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import scala.collection.{GenMap, GenSeq}

import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

object AltPathSelectionProblem {
  type PersonID = String
  type GlobalAlternates = GenMap[PersonID, GenMap[Int, (LocalODPair, List[SORoutingPathSegment])]]
  /**
    * MCTS 'Action' Type [A] for tree search, an alternate path represented by a label
    * @param personId the name of the person associated with some set of alternates
    * @param alternate the position of this alternate in the Seq[Path] that has the edges used
    */
  case class Tag(personId: PersonID, alternate: Int)

  /**
    * MCTS 'State' Type [S] for tree search, a collection of alternate paths
    */
  type AlternatesSet = Seq[Tag]

  def createTagFrom(localODPair: LocalODPair, index: Int): Tag = Tag(localODPair.id, index)

  /**
    * creates a global set of alternate path data which can be inspected by Tags to find relevant data
    * @param request
    * @return
    */
  def repackage(request: GenMap[LocalODPair, GenSeq[List[SORoutingPathSegment]]]): GlobalAlternates = {
    request
      .map {
        outerTuple =>
          (outerTuple._1.id: PersonID) -> outerTuple._2.zipWithIndex.map {
            innerTuple =>
              innerTuple._2 -> (outerTuple._1, innerTuple._1)
          }.toMap
      }
  }

  // TODO: make inspectors that go into GlobalAlternates and grab (LocalODPair, Path) tuples

  def grabOriginalsAssociatedWith(tag: Tag, alts: GlobalAlternates): (LocalODPair, List[SORoutingPathSegment]) = {
    for {
      alt <- alts.get(tag.personId)
      // TODO: here, then below
    }
  }

  def unTagAlternates(solution: AlternatesSet): GenMap[LocalODPair, List[SORoutingPathSegment]] = {
    for {
      tag <- solution

    }
  }
}
