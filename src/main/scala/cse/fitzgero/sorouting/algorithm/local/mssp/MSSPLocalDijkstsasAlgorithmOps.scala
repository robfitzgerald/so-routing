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

  // scale the cost difference values by 1000 so we don't lose precision information
  val Scalar: Long = 1000L

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
              // TODO: something is screwy here. getting negative cost differences, which shouldn't be possible.
              for {
                costAfterAdding <- edge.attribute.costFlow(eTup._2)
                costBeforeAdding <- edge.attribute.linkCostFlow
                if costAfterAdding > costBeforeAdding
              } yield {
                costAfterAdding - costBeforeAdding
              }
            case None => None
          }
        }).sum

    Math.round(costDifference * Scalar)
  }

}
