package cse.fitzgero.sorouting.experiments

import java.nio.file.{Files, Paths}

import cse.fitzgero.sorouting.model.population.LocalRequest

import scala.annotation.tailrec
import scala.collection.{GenMap, GenSeq}
import scala.util.{Failure, Random, Success, Try}
import scala.io.Source

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

  /**
    * write a log to a file
    * @param log a log
    * @param filePath it's destination file path (absolute or relative)
    */
  def writeLog(log: Map[String, Long], filePath: String, name: String = "log.txt"): Unit = {
    Try({
      val fileData: String = log.toSeq.sortBy(_._1).map(tup => s"${tup._1} ${tup._2}").mkString("\n")

      Files.write(Paths.get(s"$filePath/$name"), fileData.getBytes)
    }) match {
      case Success(_) =>
      case Failure(e) => println(s"unable to write log to file $filePath: ${e.getMessage}")
    }
  }

  def loadLog(path: String): Map[String, Long] = {
    Try({
      Source.fromFile(path)
    }) match {
      case Success(file) =>
        val tokens = file.getLines.map(_.split(" "))
        tokens.map(arr => arr(0) -> arr(1).toLong).toMap
      case Failure(e) =>
        println(s"[loadLog] unable to load $path: ${e.getMessage}")
        Map.empty[String, Long]
    }
  }
}

