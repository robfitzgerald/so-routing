package cse.fitzgero.sorouting.experiments.steps

import scala.collection.{GenIterable, GenMap, GenSeq}
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Try}
import scala.xml.XML
import scala.concurrent.duration._

import cse.fitzgero.graph.config.KSPBounds
import cse.fitzgero.sorouting.algorithm.local.mksp.MKSPLocalDijkstrasService
import cse.fitzgero.sorouting.algorithm.local.selection.SelectionLocalCombinatorialService
import cse.fitzgero.sorouting.experiments.ops.ExperimentStepOps
import cse.fitzgero.sorouting.model.population.{LocalPopulationNormalGenerator, LocalRequest}
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BPRCostFunctionType
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalEdge, LocalGraph, LocalGraphOps}
import edu.ucdenver.fitzgero.lib.experiment.{ExperimentGlobalLog, ExperimentStepLog, StepStatus, SyncStep}

object SelectionAlgorithmBenchmark {

  type BenchmarkConfig = {
    def experimentInstanceDirectory: String // directory for the data stored for this benchmark
  }

  object CreateBenchmark extends SyncStep {

    sealed trait CreateBenchmarkNetworkFlows {
      def generateFlow: Int
    }
    case object NoNetworkFlows extends CreateBenchmarkNetworkFlows {
      override def generateFlow: Int = 0
    }
    // if you make this, you need to also deal with generalizing snapshot file creation and loading
//    case class GenerateRandomNetworkFlows(
//      max: Int, min: Int = 0
//    ) extends CreateBenchmarkNetworkFlows {
//      val r = scala.util.Random
//      require(min > -1, "CreateBenchmarkNetworkFlows.min must be 0 or greater.")
//      override def generateFlow: Int = r.nextInt(max - min) + min
//    }

    type CreateBenchmarkConfig = BenchmarkConfig {
      def sourceAssetsDirectory: String // location of the network.xml file
      def k: Int // number of alternate paths targeted per driver
      def kspBounds: Option[KSPBounds] // way we terminate our ksp search
      def overlapThreshold: Double // what percent of edges alternates are permitted to have in common
      def batchSize: Int // number of people in this routing batch
      def timeWindow: Int // the window size, used in the calculation of the rate of change in the cost function
      def networkFlows: CreateBenchmarkNetworkFlows
    }

    override type StepConfig = CreateBenchmarkConfig
    override val name: String = "[SelectionAlgorithmBenchmark:CreateBenchmark] Generates a test case for combinatorial selection algorithm benchmarking"

    override def apply(config: StepConfig, log: ExperimentGlobalLog): Option[(StepStatus, ExperimentStepLog)] = Some {
      val t: Try[ExperimentStepLog] =
        Try[ExperimentStepLog] {

          // given a network, some batch size, and network flow information
          val graph: LocalGraph = {
            val networkXML = XML.loadFile(s"${config.sourceAssetsDirectory}/network.xml")
            val asGraph = LocalGraphOps.readMATSimXML(networkXML, None, BPRCostFunctionType, config.timeWindow)
            asGraph.edges.keys.foldLeft(asGraph)((graph, edgeId) => {
              graph.edgeById(edgeId) match {
                case None => graph
                case Some(edge) =>
                  val updated = LocalEdge.modifyFlow(edge, config.networkFlows.generateFlow)
                  graph.updateEdge(edgeId, updated)
              }
            })
          }

          // load the population for this single batch
          val populationXML: xml.Elem = XML.load(s"${config.experimentInstanceDirectory}/population.xml")
          // TODO: LocalPopulationNormalGenerator inherits from the trait where fromXML lives; should be accessible from an object/singleton
          val population: GenSeq[LocalRequest] = LocalPopulationNormalGenerator.fromXML(populationXML)


          case class MKSPServiceConfig(k: Int, kspBounds: Option[KSPBounds], overlapThreshold: Double)

          val f: Future[Map[String, String]] =
            for {
            ksp <- MKSPLocalDijkstrasService.runService(graph, population, Some(MKSPServiceConfig(config.k, config.kspBounds, config.overlapThreshold)))
            kspResult <- ksp map { _.result }
            selectionResult <- SelectionLocalCombinatorialService.runService(graph, kspResult, None)
            selection <- selectionResult
          } yield {
            // set up a file, or files for these results (xml?) that contains
            //   the graph used
            //   the expected cost value of the graph via this addition

            // evaluate the cost
            // TODO: code lifted from SelectionLocalMCTSAlgorithm, should be in util object
            val edgesAndFlows: GenMap[String, Int] =
              selection.result
                .flatMap {
                  _.path.map {
                    _.edgeId
                  }
                }.groupBy(identity).mapValues(_.size)

            //   the cost delta resulting from the group selected
            val costDifference: Double = {
              for {
                e <- edgesAndFlows
                edge <- graph.edgeById(e._1)
                //        currentEdgeFlow <- edge.attribute.flow
                previousCost <- edge.attribute.costFlow(e._2 - 1)
                updatedCost <- edge.attribute.costFlow(e._2)
                if e._2 != 0
              } yield updatedCost - previousCost
            }.sum

            //   the selected alternate path set

            // we want utilities that can be called again that are related to this
            // such as load these things, write these things

            Map("" -> "")
          }


          Await.result(f, 30 days)
        }
      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }
}
