package cse.fitzgero.sorouting.algorithm

import cse.fitzgero.sorouting.matsimrunner.population.PersonIDType
import cse.fitzgero.sorouting.roadnetwork.edge.{EdgeIdType, MacroscopicEdgeProperty}
import org.apache.spark.graphx.{Graph, VertexId}

package object shortestpath {

  type Path = List[EdgeIdType]

  /**
    * Part of the Pregel message which, for some OD pair, captures the weight and path information for the current Pregel iteration
    * @param weight total path cost for this OD pair, at this iteration (up to whatever intermediary vertex the message has traveled)
    * @param path associated with the (possibly partial) weight, this is the possibly partial path traveled
    */
  case class SPGraphMsg(personId: PersonIDType, weight: Double = Double.PositiveInfinity, path: Path = List.empty[EdgeIdType])

  /**
    * Type of the Vertex Data of the Shortest Paths Graph
    */
  type SPGraphData = Map[VertexId, SPGraphMsg]
  type ShortestPathsGraph = Graph[SPGraphData, MacroscopicEdgeProperty]

  case class ODPair(personId: PersonIDType, srcVertex: VertexId, dstVertex: VertexId)
  type ODPairs = Seq[ODPair]
  case class ODPath(personId: PersonIDType, srcVertex: VertexId, dstVertex: VertexId, path: Path)
  type ODPaths = Seq[ODPath]

  /**
    * CostMethods are enumeration objects to identify which type of shortest path cost evaluation we are seeking
    */
  sealed trait CostMethod
  case class CostFlow() extends CostMethod
  case class AONFlow() extends CostMethod
}
