package cse.fitzgero.sorouting.algorithm.pathselection

import cse.fitzgero.sorouting.algorithm.pathsearch.od.ODPath
import cse.fitzgero.sorouting.roadnetwork.RoadNetwork

import scala.collection.GenSeq
import scala.concurrent.Future

trait PathSelection [O <: ODPath[_,_], G <: RoadNetwork] {
  // define the return type, to allow for easy matching on results from PathSelection subclasses
  type Result <: PathSelectionFound[O]
  /**
    * given a set of alternate paths for each O/D pair, select a best fit, and return the set of best fit paths
    * @param set for each OD Pair, a set of alternate paths
    * @param graph a road network. we will add flows for each possible combination of these sets, and return the set with each driver that has the minimal cost
    */
  def run (set: GenSeq[GenSeq[O]], graph: G): Future[PathSelectionResult]
}
