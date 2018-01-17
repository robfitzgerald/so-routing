package cse.fitzgero.sorouting.algorithm.local.selection

import scala.collection.{GenMap, GenSeq}

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

import cse.fitzgero.graph.algorithm.GraphAlgorithm
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.model.path.SORoutingPathSegment
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.CostFunction
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalEdgeAttribute, LocalGraph, LocalODPair}

object SelectionSparkCombinatorialAlgorithm extends GraphAlgorithm {
  override type VertexId = KSPLocalDijkstrasAlgorithm.VertexId
  override type EdgeId = KSPLocalDijkstrasAlgorithm.EdgeId
  override type Graph = KSPLocalDijkstrasAlgorithm.Graph
  type Path = List[SORoutingPathSegment]
  override type AlgorithmRequest = GenMap[LocalODPair, GenSeq[Path]]
  type SSSPAlgorithmResult = KSPLocalDijkstrasAlgorithm.AlgorithmResult
  override type AlgorithmConfig = SparkContext
  override type AlgorithmResult = GenMap[LocalODPair, Path]

  // get or else this value when finding edge.costFlow
  val DefaultFlowCost: Double = 0D

  // only parallelize finding the minimum cost combination when there are more than this many combinations
  val CombinationsParallelizationThreshold: Long = 2000L

  // a tuple to represent OD pairs
  case class Tag(personId: String, alternate: Int) extends Serializable {
    override def toString: String = s"($personId#$alternate)"
  }


  override def runAlgorithm(graph: LocalGraph, request: GenMap[LocalODPair, GenSeq[Path]], config: Option[SparkContext]): Option[GenMap[LocalODPair, Path]] = {
    config match {
      case None => None
      case Some(sc) =>
        if (request.isEmpty) {
          None
        } else {
          // a back-tracking map from personIds to their OD object
          val unTag: GenMap[String, LocalODPair] =
            request.keys.map(od => (od.id, od)).toMap

          // a multiset of path sets for each person as a Spark RDD
          val sequentialRequest: Seq[Seq[(Tag, Path)]] = tagRequests(request)

          // generate all combinations and find the minimal cost combination
          val combinations = generateAllCombinations(sc)(sequentialRequest)
          val expectedCombinations: Long = request.map(_._2.size.toLong).product

          minimalCostCombination(sc)(combinations, graph, expectedCombinations) match {
            case None => None
            case Some(minimalSet) =>
              val result = minimalSet.map { minimal => (unTag(minimal._1.personId), minimal._2) }

//              println("finishing running SparkCombinatorial with request, response:")
//              request.foreach(req => println(s"${req._1} with ${req._2.size} alt paths"))
//              result.foreach(res => println(s"${res._1} with selected path ${res._2.map(_.edgeId).mkString("(","->",")")}"))
//              println()

              Some(result.toMap)
          }
        }
    }
  }


  def generateAllCombinations(sc: SparkContext)(set: Seq[Seq[(Tag, Path)]]): RDD[Seq[(Tag, Path)]] = {

    def subCombinations(subSet: Seq[Seq[(Tag, Path)]], solution: Seq[(Tag, Path)] = Seq()): Seq[Seq[(Tag, Path)]] = {
      if (subSet.isEmpty) Seq(solution)
      else {
        subSet.head.flatMap { alt => subCombinations(subSet.tail, alt +: solution) }
      }
    }

    if (set.isEmpty) sc.emptyRDD[Seq[(Tag, Path)]]
    else if (set.size == 1) sc.parallelize(set)
    else {

      // TODO: remove this spark-ficiation here. redo it as sequential, then parallelize the 1x2 result below if needed (current solution is an over-engineering, isn't it?)
      // perform all combinations for request 1 and 2
      val sortedBySizeDescending: Vector[Seq[(Tag, Path)]] = set.toVector.sortBy(-_.size)
      val oneAndTwo: Seq[Seq[(Tag, Path)]] = Seq(sortedBySizeDescending(0), sortedBySizeDescending(1))

      val oneAndTwoCombinations: RDD[Seq[(Tag, Path)]] = sc.parallelize(subCombinations(oneAndTwo))
//       if there are two requests, then return this result
      if (set.size == 2) oneAndTwoCombinations
      else {
        val remaining: Seq[Seq[(Tag, Path)]] = sortedBySizeDescending.drop(2)
        // for each combination of 1 and 2, apply the recursive combinations solver
        oneAndTwoCombinations.flatMap {
          oneAndTwoCombination: Seq[(Tag, Path)] =>
            subCombinations(remaining, oneAndTwoCombination)
        }
      }

      // if not, convert it into a spark job and run subcombinations on each element

      // req1 and req2 should be as large as possible for uniform parallelism


//      // cartesian product to produce all combinations of the first two requests
//      val req1 = sc.parallelize(sortedBySizeDescending(0))
//      val req2 = sc.parallelize(sortedBySizeDescending(1))
//      val combinations = req1.cartesian(req2).map { tup => Seq(tup._1, tup._2) } // TODO: .partitionBy(hashPartitioner(partitions)) ?
//
//      if (set.size == 2) combinations
//      else {
//        val remaining: Seq[Seq[(Tag, Path)]] = set.tail.tail
//        // for each combination of 1 and 2, apply the recursive combinations solver
//        combinations.flatMap {
//          oneAndTwoCombination: Seq[(Tag, Path)] =>
//            subCombinations(remaining, oneAndTwoCombination)
//        }
//      }
    }
  }


  def minimalCostCombination(sc: SparkContext)(combinations: RDD[Seq[(Tag, Path)]], graph: LocalGraph, expectedCombinations: Long): Option[Seq[(Tag, Path)]] = {
    if (expectedCombinations == 0) {
      None
    } else if (expectedCombinations < CombinationsParallelizationThreshold) {
      // find minimum using local method
      val edgeCostLookup: GenMap[String, LocalEdgeAttribute with CostFunction] = edgeLookup(graph)
      val minimum =
        combinations
          .collect()
          .map { combination =>
            val cost: Double = costOfCombinationSet(edgeCostLookup)(edgesVisitedInSet(combination))
            (cost, combination)
          }
          .minBy(_._1)._2
      Some(minimum)
    } else {
      // find minimum using spark aggregate method
      val edgeCostLookup: Broadcast[GenMap[String, LocalEdgeAttribute with CostFunction]] =
        sc.broadcast(edgeLookup(graph))

      val zero = Seq.empty[(Double, Seq[(Tag, Path)])]
      val minimum: Seq[(Double, Seq[(Tag, Path)])] =
        combinations
          .aggregate(zero)(
            (acc: Seq[(Double, Seq[(Tag, Path)])], elem: Seq[(Tag, Path)]) => {
              val cost = costOfCombinationSet(edgeCostLookup.value)(edgesVisitedInSet(elem))
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
        Some(minimum.head._2)
      else None
    }
  }


  def trivialCostSelection(request: GenMap[LocalODPair, GenSeq[Path]], graph: LocalGraph): Option[(LocalODPair, Path)] = {
    if (request.size != 1) None
    else {
      val edgeCostLookup = edgeLookup(graph)
      val onlyPerson = request.head
      val bestPath =
        onlyPerson._2
          .map{
            path: Path =>
              val cost = path.map(e => edgeCostLookup(e.edgeId).costFlow(1).getOrElse(0D)).sum
              (path, cost)
          }
          .minBy(_._2)
          ._1
      Some((onlyPerson._1, bestPath))
    }
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


  def edgeLookup(graph: LocalGraph): GenMap[String, LocalEdgeAttribute with CostFunction] =
    graph.edges.map(edgeRow => (edgeRow._1, edgeRow._2.attribute))
}
