package cse.fitzgero.sorouting.algorithm.local.selection

import scala.collection.{GenMap, GenSeq}

import cse.fitzgero.graph.algorithm.GraphAlgorithm
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.CostFunction
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalEdgeAttribute, LocalGraph, LocalODPair}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{HashPartitioner, SparkConf, SparkContext}

object SelectionSparkCombinatorialAlgorithm extends GraphAlgorithm {
  override type VertexId = KSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = KSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = KSPLocalDijkstrasAlgorithm.Graph
  type Path = List[SORoutingPathSegment]
  override type AlgorithmRequest = GenMap[LocalODPair, GenSeq[Path]]
  type SSSPAlgorithmResult = KSPLocalDijkstrasAlgorithm.AlgorithmResult
  override type AlgorithmConfig = SparkContext

  val DefaultFlowCost: Double = 0D
  // a tuple to represent OD pairs
  case class Tag(personId: String, alternate: Int) extends Serializable


  override type AlgorithmResult = GenMap[LocalODPair, Path]

  override def runAlgorithm(graph: LocalGraph, request: GenMap[LocalODPair, GenSeq[Path]], config: Option[SparkContext]): Option[GenMap[LocalODPair, Path]] = {
    config match {
      case None => None
      case Some(sc) =>
        // a back-tracking map from personIds to their OD object
        val unTag: GenMap[String, LocalODPair] =
          request.keys.map(od => (od.id, od)).toMap

        // a multiset of path sets for each person as a Spark RDD
        val sequentialRequest: Seq[Seq[(Tag, Path)]] = tagRequests(request)

        val result: Seq[(LocalODPair, Path)] = for {
          minimal <- minimalCostCombination(sc)(generateAllCombinations(sc)(sequentialRequest), graph)
        } yield {
          (unTag(minimal._1.personId), minimal._2)
        }

        if (result.size == request.size)
          Some(result.toMap)
        else
          None
    }
  }


  def generateAllCombinations(sc: SparkContext)(set: Seq[Seq[(Tag, Path)]]): RDD[Seq[(Tag, Path)]] = {
    def subCombinations(subSet: Seq[Seq[(Tag, Path)]], solution: Seq[(Tag, Path)]): Seq[Seq[(Tag, Path)]] = {
      if (subSet.isEmpty) Seq(solution)
      else {
        subSet.head.flatMap { alt => subCombinations(subSet.tail, alt +: solution) }
      }
    }

    if (set.isEmpty) sc.emptyRDD[Seq[(Tag, Path)]]
    else if (set.size == 1) sc.parallelize(set)
    else {
      // req1 and req2 should be as large as possible for uniform parallelism
      val sortedBySizeDescending = set.toVector.sortBy(-_.size)
      val partitions: Int = sortedBySizeDescending(0).size * sortedBySizeDescending(1).size
      val req1 = sc.parallelize(sortedBySizeDescending(0))
      val req2 = sc.parallelize(sortedBySizeDescending(1))
      val combinations = req1.cartesian(req2).map { tup => Seq(tup._1, tup._2) } // TODO: .partitionBy(hashPartitioner(partitions)) ?
      if (set.size == 2) combinations
      else {
        val remaining: Seq[Seq[(Tag, Path)]] = set.tail.tail
        combinations.flatMap {
          oneAndTwoCombination: Seq[(Tag, Path)] =>
            subCombinations(remaining, oneAndTwoCombination)
        }
      }
    }
  }


  def minimalCostCombination(sc: SparkContext)(combinations: RDD[Seq[(Tag, Path)]], graph: LocalGraph): Seq[(Tag, Path)] = {
    // edge data by edge id stored as a Spark RDD
    val edgeLookup: Broadcast[GenMap[String, LocalEdgeAttribute with CostFunction]] =
      sc.broadcast(graph.edges.map(edgeRow => (edgeRow._1, edgeRow._2.attribute)))
    val zero = Seq.empty[(Double, Seq[(Tag, Path)])]
    val minimum: Seq[(Double, Seq[(Tag, Path)])] =
      combinations
        .aggregate(zero)(
          (acc: Seq[(Double, Seq[(Tag, Path)])], elem: Seq[(Tag, Path)]) => {
            val cost = costOfCombinationSet(edgeLookup.value)(edgesVisitedInSet(elem))
            (cost, elem) +: acc
          },
          (a: Seq[(Double, Seq[(Tag, Path)])], b: Seq[(Double, Seq[(Tag, Path)])]) => {
            a ++ b match {
              case Nil => Seq()
              case c: Seq[(Double, Seq[(Tag, Path)])] =>
                Seq(c.minBy(_._1))
            }
          }
        )
    if (minimum.nonEmpty)
      minimum.head._2
    else Seq()
  }


  def tagRequests(request: GenMap[LocalODPair, GenSeq[Path]]): Seq[Seq[(Tag, Path)]] =
    request
      .map {
        od =>
          od._2.zipWithIndex
            .map {
              path =>
                (Tag(od._1.id, path._2), path._1)
            }
            .toList
      }
      .toList


  def edgesVisitedInSet(combination: Seq[(Tag, Path)]): Map[EdgeId, Int] =
    combination
      .flatMap(_._2.map(edge => (edge.edgeId, 1)))
      .groupBy(_._1)
      .mapValues(_.map(_._2).sum)


  def costOfCombinationSet(lookup: GenMap[String, LocalEdgeAttribute with CostFunction])(combination: Map[EdgeId, Int]): Double =
    combination.map {
      edgeAndFlow =>
        lookup(edgeAndFlow._1)
          .costFlow(edgeAndFlow._2)
          .getOrElse(DefaultFlowCost) // TODO: add policy for managing missing cost flow evaluation data (is zero the correct default value?)
      }.sum
}
