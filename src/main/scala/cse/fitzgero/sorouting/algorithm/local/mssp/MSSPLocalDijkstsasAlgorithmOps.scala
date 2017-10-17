package cse.fitzgero.sorouting.algorithm.local.mssp

import cse.fitzgero.sorouting.algorithm.local.sssp.SSSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.population.LocalResponse

import scala.collection.{GenIterable, GenMap, GenSeq}

object MSSPLocalDijkstsasAlgorithmOps { ops =>
  type EdgeId = String
  type VertexId = String
//  type Path = List[SORoutingPathSegment]
  type Graph = SSSPLocalDijkstrasAlgorithm.Graph {
    type EdgeId = ops.EdgeId
    type VertexId = ops.VertexId
  }

  /**
    * given a graph and a set of paths, calculate the cost that would be added to the network
    * @param graph a road network
    * @param paths a set of paths for each request in a batch-oriented path algorithm
    * @return the difference between the previous graph link costs and the links when these paths are added
    */
  def calculateAddedCost(graph: Graph, paths: GenSeq[LocalResponse]): Double = {
    val edgeIdAndFlow: GenMap[EdgeId, Int] =
      paths
        .flatMap(_.path.map(_.edgeId))
        .groupBy(identity)
        .mapValues(_.size)

    val costDifference: Double =
      edgeIdAndFlow
        .flatMap(eTup => {
          graph.edgeById(eTup._1) match {
            case Some(edge) =>
              Some(edge.attribute.costFlow(eTup._2).getOrElse(0D) - edge.attribute.linkCostFlow.getOrElse(0D))
            case None => None
          }
        }).sum

    Math.round(costDifference)
  }

}
