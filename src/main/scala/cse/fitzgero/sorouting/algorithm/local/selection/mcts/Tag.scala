package cse.fitzgero.sorouting.algorithm.local.selection.mcts

import scala.collection.{GenMap, GenSeq}

import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

/**
  * MCTS 'Action' Type [A] for tree search, an alternate path represented by a label
  * @param personId the name of the person associated with some set of alternates
  * @param alternate the position of this alternate in the Seq[Path] that has the edges used
  */
case class Tag(personId: Tag.PersonID, alternate: Int)

object Tag {
  type PersonID = String
  type GlobalAlternates = GenMap[PersonID, GenMap[Int, (LocalODPair, List[SORoutingPathSegment])]]


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

  def grabOriginalsAssociatedWith(tag: Tag, alts: GlobalAlternates): Option[(LocalODPair, List[SORoutingPathSegment])] = {
    for {
      thisPersonsAlts <- alts.get(tag.personId)
      thisAlt <- thisPersonsAlts.get(tag.alternate)
    } yield {
      thisAlt
    }
  }

  def unTagAlternates(solution: AlternatesSet, globalAlts: GlobalAlternates): GenMap[LocalODPair, List[SORoutingPathSegment]] = {
    (for {
      tag <- solution
      alt <- grabOriginalsAssociatedWith(tag, globalAlts)
    } yield {
      alt
    }).toMap
  }
}
