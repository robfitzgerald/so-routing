package cse.fitzgero.sorouting.algorithm.pathsearch.mssp.graphx

import cse.fitzgero.sorouting.matsimrunner.population.PersonIDType
import cse.fitzgero.sorouting.roadnetwork.graphx.edge.{EdgeIdType, MacroscopicEdgeProperty}
import org.apache.spark.graphx.{Graph, VertexId}

package object simplemssp {
  type Path = List[EdgeIdType]

  /**
    * Part of the Pregel od which, for some OD pair, captures the weight and path information for the current Pregel iteration
    * @param weight total path cost for this OD pair, at this iteration (up to whatever intermediary vertex the od has traveled)
    * @param path associated with the (possibly partial) weight, this is the possibly partial path traveled
    */
  case class SimpleMSSP_PregelMsg(weight: Double = Double.PositiveInfinity, path: Path = List.empty[EdgeIdType])

  /**
    * Type of the Vertex Data of the Shortest Paths Graph
    */
  type SimpleMSSG_PregelVertex = Map[VertexId, SimpleMSSP_PregelMsg]
  type ShortestPathsGraph = Graph[SimpleMSSG_PregelVertex, MacroscopicEdgeProperty]

  case class SimpleMSSP_ODPair(personId: PersonIDType, srcVertex: VertexId, dstVertex: VertexId) extends GraphXODPair
  type ODPairs = Seq[SimpleMSSP_ODPair]
  case class SimpleMSSP_ODPath(personId: PersonIDType, srcVertex: VertexId, dstVertex: VertexId, path: Path) extends GraphXODPath
  type ODPaths = Seq[SimpleMSSP_ODPath]

  /**
    * CostMethods are enumeration objects to identify which type of shortest path cost evaluation we are seeking
    */
  sealed trait CostMethod
  case class CostFlow() extends CostMethod
  case class AONFlow() extends CostMethod
}
