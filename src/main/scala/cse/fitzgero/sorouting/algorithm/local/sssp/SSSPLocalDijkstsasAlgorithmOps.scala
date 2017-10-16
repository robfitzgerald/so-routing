package cse.fitzgero.sorouting.algorithm.local.sssp

import scala.collection.{GenIterable, GenMap}

object SSSPLocalDijkstsasAlgorithmOps { ops =>
  type EdgeId = String
  type VertexId = String
  type Path = SSSPLocalDijkstrasAlgorithm.Path
  type Graph = SSSPLocalDijkstrasAlgorithm.Graph {
    type EdgeId = ops.EdgeId
    type VertexId = ops.VertexId
  }

  def calculateAddedCost(graph: Graph, paths: GenIterable[Path]): Double = {
    val edgeIdAndFlow: GenMap[EdgeId, Int] =
      paths
        .flatMap(_.map(_.e))
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
