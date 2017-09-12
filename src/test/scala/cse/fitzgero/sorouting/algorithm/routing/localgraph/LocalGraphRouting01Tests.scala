package cse.fitzgero.sorouting.algorithm.routing.localgraph

import java.time.LocalTime

import scala.xml.XML
import cse.fitzgero.sorouting.SORoutingAsyncUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.NoKSPBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPairByVertex
import cse.fitzgero.sorouting.algorithm.routing._
import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.matsimrunner.population._
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{LocalGraphMATSim, LocalGraphMATSimFactory}
import cse.fitzgero.sorouting.util.convenience._

class LocalGraphRouting01Tests extends SORoutingAsyncUnitTestTemplate {
  "LocalGraphRouting" when {
    val networkFilePath =            "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-network.xml"
    val snapshotFilePath =           "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-snapshot.xml"
    val ryeNetworkFilePath =         "src/main/resources/matsimNetworks/RyeNetwork.xml"
    val fiveByFiveNetworkFilePath =         "src/main/resources/matsimNetworks/5X5Network.xml"
    def config(pop: Int) =
      RandomPopulationOneTripConfig(
        pop,
        Seq(
          ActivityConfig2(
            "home",
            LocalTime.parse("08:00:00"),
            30L),
          ActivityConfig2(
            "work",
            LocalTime.parse("17:00:00"),
            30L),
          ActivityConfig2(
            "home",
            LocalTime.parse("23:00:00"),
            30L)
        ),
        Seq(ModeConfig("car"))
      )
    "route" when {
//      "called with a road network, valid od pairs, and a local processing config" should {
//        "produce the correct set of system-optimal routes" in {
//          import scala.concurrent.ExecutionContext.Implicits.global
//
//          val config = LocalRoutingConfig(4, NoKSPBounds, IterationTerminationCriteria(20 iterations))
//          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10D).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
//          val odPairs: Seq[LocalGraphODPairByVertex] = PopulationOneTrip(Set()).updatePerson(LocalGraphODPairByVertex("", 1L, 11L))
//          LocalGraphRouting.route(graph, odPairs, config) map {
//            case LocalGraphRoutingResult(res, _) =>
//              res.head.path should equal (List("1-3","3-5","5-9","9-8","8-11"))
//            case _ =>
//              fail()
//          }
//        }
//      }
      "called on a 5x5 graph with a gaussian population distribution" should {
        "run faster with a parallel processing" in {
          import scala.concurrent.ExecutionContext.Implicits.global
          val terminationCriteria =
            CombinedFWBounds(
              RunningTimeFWBounds(15 seconds),
              Or,
              IterationFWBounds(10 iterations))
          val localConfig = LocalRoutingConfig(10, NoKSPBounds, terminationCriteria)
          val parConfig = ParallelRoutingConfig(10, NoKSPBounds, terminationCriteria)
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFile(fiveByFiveNetworkFilePath).get
          val fiveByFiveNetworkXML = XML.loadFile(fiveByFiveNetworkFilePath)
          PopulationMultipleTripsFactory.setSeed(1)
          val population = PopulationOneTrip.generateRandomOneTripPopulation(fiveByFiveNetworkXML, config(200 persons))
          LocalGraphRouting01.route(graph, population, localConfig) flatMap {
            case LocalGraphRoutingResult(resLocal, _, _, _, runTimeLocal) =>
              LocalGraphRouting01.route(graph, population, parConfig) map {
                case LocalGraphRoutingResult(resPar, _, _, _, runTimePar) =>
                  println(s"runTimePar: $runTimePar runTimeLocal: $runTimeLocal")
                  runTimePar should be < runTimeLocal
                case _ => fail()
              }
            case _ => fail()
          }
        }
      }
      // TODO: this should be in a benchmark, not in a unit test
      "called on a real-sized road network with a generated population of 20, producing 10 different paths" should {
        "run faster with a parallel processing" ignore {
          import scala.concurrent.ExecutionContext.Implicits.global
          val terminationCriteria =
            CombinedFWBounds(
              RunningTimeFWBounds(15 seconds),
              Or,
              IterationFWBounds(5 iterations))
          val localConfig = LocalRoutingConfig(4, NoKSPBounds, terminationCriteria)
          val parConfig = ParallelRoutingConfig(4, NoKSPBounds, terminationCriteria)
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, AlgorithmFlowRate = 10).fromFile(ryeNetworkFilePath).get
          val puebloNetworkXML = XML.loadFile(ryeNetworkFilePath)
          val population = PopulationOneTrip.generateRandomOneTripPopulation(puebloNetworkXML, config(20 persons))
          LocalGraphRouting01.route(graph, population, localConfig) flatMap {
            case LocalGraphRoutingResult(resLocal, _, _, _, runTimeLocal) =>
              LocalGraphRouting01.route(graph, population, parConfig) map {
                case LocalGraphRoutingResult(resPar, _, _, _, runTimePar) =>
                  println(s"networkSize: ${graph.edges.size} edges; runTimePar: ${runTimePar/1000D} secs; runTimeLocal: ${runTimeLocal/1000D} secs")
                  runTimePar should be < runTimeLocal
                case _ => fail()
              }
            case _ => fail()
          }
        }
      }
    }
  }
}
