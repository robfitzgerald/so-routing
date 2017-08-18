package cse.fitzgero.sorouting.algorithm.routing.localgraphrouting

import java.time.LocalTime

import scala.xml.XML
import cse.fitzgero.sorouting.SORoutingAsyncUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.NoKSPBounds
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPair
import cse.fitzgero.sorouting.algorithm.routing._
import cse.fitzgero.sorouting.algorithm.trafficassignment._
import cse.fitzgero.sorouting.matsimrunner.population.{PopulationMultipleTrips, PopulationMultipleTripsFactory}
import cse.fitzgero.sorouting.roadnetwork.costfunction.BPRCostFunction
import cse.fitzgero.sorouting.roadnetwork.localgraph.{LocalGraphMATSim, LocalGraphMATSimFactory}
import cse.fitzgero.sorouting.util.convenience._

class LocalGraphRoutingTests extends SORoutingAsyncUnitTestTemplate {
  "LocalGraphRouting" when {
    val networkFilePath =            "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-network.xml"
    val snapshotFilePath =           "src/test/resources/SimpleKSPTests/ksp-simple-alternate-routes-snapshot.xml"
    val ryeNetworkFilePath =         "src/main/resources/matsimNetworks/RyeNetwork.xml"
    val fiveByFiveNetworkFilePath =         "src/main/resources/matsimNetworks/5X5Network.xml"

    "route" when {
      "called with a road network, valid od pairs, and a local processing config" should {
        "produce the correct set of system-optimal routes" in {
          import scala.concurrent.ExecutionContext.Implicits.global
          val config = LocalRoutingConfig(4, NoKSPBounds, IterationTerminationCriteria(5 iterations))
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, 3600D, 10D).fromFileAndSnapshot(networkFilePath, snapshotFilePath).get
          val odPairs: Seq[LocalGraphODPair] = Seq(LocalGraphODPair("", 1L, 11L))
          LocalGraphRouting.route(graph, odPairs, config) map {
            case LocalGraphRoutingResult(res, _) =>
              println("result")
              println(res)
              succeed
            case _ => fail()
          }
        }
      }
      "called on a K5 graph with a gaussian population distribution" should {
        "run faster with a parallel processing" in {
          import scala.concurrent.ExecutionContext.Implicits.global
          val terminationCriteria =
            CombinedTerminationCriteria(
              RunningTimeTerminationCriteria(15 seconds),
              Or,
              IterationTerminationCriteria(10 iterations))
          val localConfig = LocalRoutingConfig(10, NoKSPBounds, terminationCriteria)
          val parConfig = ParallelRoutingConfig(10, NoKSPBounds, terminationCriteria)
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, 3600D, 10D).fromFile(fiveByFiveNetworkFilePath).get
          val fiveByFiveNetworkXML = XML.loadFile(fiveByFiveNetworkFilePath)
          PopulationMultipleTripsFactory.setSeed(1)
          val population: PopulationMultipleTrips = PopulationMultipleTripsFactory.generateSimpleRandomPopulation(fiveByFiveNetworkXML, 200 persons)
          val odPairsMSSP = population.exportTimeGroupAsODPairs(LocalTime.parse("06:00:00"), LocalTime.parse("12:00:00"))
          val odPairs = odPairsMSSP.map(od => {LocalGraphODPair(od.personId, od.srcVertex, od.dstVertex)})
          LocalGraphRouting.route(graph, odPairs, localConfig) flatMap {
            case LocalGraphRoutingResult(resLocal, runTimeLocal) =>
              LocalGraphRouting.route(graph, odPairs, parConfig) map {
                case LocalGraphRoutingResult(resPar, runTimePar) =>
                  println(s"runTimePar: $runTimePar runTimeLocal: $runTimeLocal")
                  runTimePar should be < runTimeLocal
                case _ => fail()
              }
            case _ => fail()
          }
        }
      }
      "called on a real-sized road network with a generated population of 20, producing 10 different paths" should {
        "run faster with a parallel processing" in {
          import scala.concurrent.ExecutionContext.Implicits.global
          val terminationCriteria =
            CombinedTerminationCriteria(
              RunningTimeTerminationCriteria(15 seconds),
              Or,
              IterationTerminationCriteria(5 iterations))
          val localConfig = LocalRoutingConfig(4, NoKSPBounds, terminationCriteria)
          val parConfig = ParallelRoutingConfig(4, NoKSPBounds, terminationCriteria)
//          val config = ParallelRoutingConfig(4, NoKSPBounds, terminationCriteria)
          val graph: LocalGraphMATSim = LocalGraphMATSimFactory(BPRCostFunction, 3600D, 10D).fromFile(ryeNetworkFilePath).get
          val puebloNetworkXML = XML.loadFile(ryeNetworkFilePath)
          PopulationMultipleTripsFactory.setSeed(1)
          val population: PopulationMultipleTrips = PopulationMultipleTripsFactory.generateSimpleRandomPopulation(puebloNetworkXML, 20 persons)
          val odPairsMSSP = population.exportTimeGroupAsODPairs(LocalTime.parse("06:00:00"), LocalTime.parse("12:00:00"))
          val odPairs = odPairsMSSP.map(od => {LocalGraphODPair(od.personId, od.srcVertex, od.dstVertex)})
          LocalGraphRouting.route(graph, odPairs, localConfig) flatMap {
            case LocalGraphRoutingResult(resLocal, runTimeLocal) =>
              LocalGraphRouting.route(graph, odPairs, parConfig) map {
                case LocalGraphRoutingResult(resPar, runTimePar) =>
                  println(s"networkSize: ${} edges; runTimePar: ${runTimePar/1000D} secs; runTimeLocal: ${runTimeLocal/1000D} secs")
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
