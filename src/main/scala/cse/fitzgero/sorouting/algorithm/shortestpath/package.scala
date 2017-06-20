package cse.fitzgero.sorouting.algorithm

import cse.fitzgero.sorouting.roadnetwork.edge.{EdgeIdType, MacroscopicEdgeProperty}
import org.apache.spark.graphx.{Graph, VertexId}

package object shortestpath {

  /**
    * Part of the Pregel message which, for some OD pair, captures the weight and path information for the current Pregel iteration
    * @param weight total path cost for this OD pair, at this iteration (up to whatever intermediary vertex the message has traveled)
    * @param path associated with the (possibly partial) weight, this is the possibly partial path traveled
    */
  case class WeightAndPath(weight: Double = Double.PositiveInfinity, path: List[EdgeIdType] = List.empty[EdgeIdType])

  /**
    * Type of the Vertex Data of the Shortest Paths Graph
    */
  type SPGraphData = Map[VertexId, WeightAndPath]
  type ShortestPathsGraph = Graph[SPGraphData, MacroscopicEdgeProperty]

  /**
    * CostMethods are enumeration objects to identify which type of shortest path cost evaluation we are seeking
    */
  sealed trait CostMethod
  case class CostFlow() extends CostMethod
  case class AONFlow() extends CostMethod
}
