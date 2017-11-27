package cse.fitzgero.sorouting.algorithm.local.selection

import java.time.LocalTime

import scala.collection.{GenMap, GenSeq}
import scala.util.{Failure, Success}

import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasAlgorithm
import cse.fitzgero.sorouting.algorithm.local.ksp.KSPLocalDijkstrasAlgorithm.Path
import cse.fitzgero.sorouting.algorithm.local.mssp.MSSPLocalDijkstrasService
import cse.fitzgero.sorouting.algorithm.local.selection.SelectionLocalMCTSAlgorithm.{MCTSAltPath, MCTSTreeNode, Tag}
import cse.fitzgero.sorouting.model.population.LocalRequest
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalODPair

class SelectionLocalMCTSAlgorithmTests extends SORoutingUnitTestTemplate {
  "SelectionMCTSAlgorithm" when {
    "runAlgorithm" when {
      "called with a small graph and two small sets of alternate paths" should {
        "find the optimal combination" in new CombinatorialTestAssets.CombinationSet {
          case class Config(coefficientCp: Double, congestionRatioThreshold: Double, computationalLimit: Long)
          val config = Config(0.7071D, 3D, 2000)
          SelectionLocalMCTSAlgorithm.runAlgorithm(graph, kspResult, Some(config)) match {
            case None => fail()
            case Some(result) =>
              println(result)
          }
        }
      }
      "called with a bigger graph and set of alternate paths" should {
        "find the optimal combination" in new CombinatorialTestAssets.BiggerMap {
          case class Config(coefficientCp: Double, congestionRatioThreshold: Double, computationalLimit: Long)
          val config = Config(0, 3.14D, 2000)
          SelectionLocalMCTSAlgorithm.runAlgorithm(bigGraph, kspResult, Some(config)) match {
            case None => fail()
            case Some(result) =>
              println(result)
              println(result.flatMap(_._2.map(_.edgeId)).groupBy(identity).mapValues(_.size).filter(_._2 > 1))
              println(s"request size ${kspResult.size} result size ${result.size}")
          }
        }
      }
      "a small square graph with 9 requests which have 3 routes to a shared destination" should {
        "find the optimal combination" in new MCTSTestAssets.graphSquare9PeopleThreeWays {
          case class KSPConfig(k: Int, kspBounds: Option[KSPBounds], overlapThreshold: Double)
          val kspConfig = KSPConfig(3, Some(KSPBounds.Iteration(4)), 1.0D)
          case class MCTSConfig(coefficientCp: Double, congestionRatioThreshold: Double, computationalLimit: Long)
          val mctsConfig = MCTSConfig(0, 1.5D, 5000)
          val altPaths: GenMap[LocalODPair, GenSeq[Path]] =
            request
              .flatMap { req => KSPLocalDijkstrasAlgorithm.runAlgorithm(graph, req.od, Some(kspConfig)) }
              .map { res => (res.od, res.paths) }
              .toMap

          altPaths.mapValues(_.map(_.map(_.edgeId).mkString(" -> ")).mkString(",")).foreach(println)

          SelectionLocalMCTSAlgorithm.runAlgorithm(graph, altPaths, Some(mctsConfig)) match {
            case None => fail()
            case Some(result) =>
              println(result)
              println(result.flatMap(_._2.map(_.edgeId)).groupBy(identity).mapValues(_.size).filter(_._2 > 1))
          }
        }
      }
      "a small graph with one alternate per vertex" should {
        "find the optimal combination" in new MCTSTestAssets.graphComposedAlts {
          case class KSPConfig(k: Int, kspBounds: Option[KSPBounds], overlapThreshold: Double)
          val kspConfig = KSPConfig(3, Some(KSPBounds.Iteration(4)), 1.0D)
          case class MCTSConfig(coefficientCp: Double, congestionRatioThreshold: Double, computationalLimit: Long)
          val mctsConfig = MCTSConfig(0, 1.5D, 5000)
          val altPaths: GenMap[LocalODPair, GenSeq[Path]] =
            request
              .flatMap { req => KSPLocalDijkstrasAlgorithm.runAlgorithm(graph, req.od, Some(kspConfig)) }
              .map { res => (res.od, res.paths) }
              .toMap

          altPaths.mapValues(_.map(_.map(_.edgeId).mkString(" -> ")).mkString(",")).foreach(println)

          SelectionLocalMCTSAlgorithm.runAlgorithm(graph, altPaths, Some(mctsConfig)) match {
            case None => fail()
            case Some(result) =>
              println(result)
              println(result.flatMap(_._2.map(_.edgeId)).groupBy(identity).mapValues(_.size).filter(_._2 > 1))
          }
        }
      }
    }
  }
}
