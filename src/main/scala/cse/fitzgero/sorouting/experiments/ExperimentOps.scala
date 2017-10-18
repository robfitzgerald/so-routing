package cse.fitzgero.sorouting.experiments

import cse.fitzgero.sorouting.model.population.LocalRequest

import scala.annotation.tailrec
import scala.collection.{GenMap, GenSeq}
import scala.util.Random

object ExperimentOps {

  /**
    * combine logs by adding values where there are common keys
    * @param a a log
    * @param b another log
    * @return one log to rule them all
    */
  def sumLogs (a: Map[String, Long], b: Map[String, Long]): Map[String, Long] = {
    val intersection = a.filter(t => b.isDefinedAt(t._1)).keySet
    val aExclusive = a.filter(t => !intersection(t._1))
    val bExclusive = b.filter(t => !intersection(t._1))
    val combineIntersection = intersection.map(i => i -> (a(i) + b(i))).toMap
    aExclusive ++ combineIntersection ++ bExclusive
  }

  /**
    * splits the population by the route percentage, placing the group corresponding to the route percentage on the left-hand side
    * @param population a set of requests
    * @param routePercentage the percentage of requests to split off to the testGroup
    * @return (testGroup, controlGroup)
    */
  def splitPopulation(population: GenSeq[LocalRequest], routePercentage: Double): (GenSeq[LocalRequest], GenSeq[LocalRequest]) = {
    val (testGroupIndexed, controlGroupIndexed) =
      Random.shuffle(population.toVector)
        .zipWithIndex
        .partition(_._2 < (routePercentage * population.size).toInt)
    (testGroupIndexed.map(_._1), controlGroupIndexed.map(_._1))
  }
}

