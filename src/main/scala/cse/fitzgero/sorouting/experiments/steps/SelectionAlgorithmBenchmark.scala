package cse.fitzgero.sorouting.experiments.steps

import scala.util.Try
import scala.xml.XML

import cse.fitzgero.sorouting.experiments.ops.ExperimentStepOps
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BPRCostFunctionType
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalGraph, LocalGraphOps}
import edu.ucdenver.fitzgero.lib.experiment.{ExperimentGlobalLog, ExperimentStepLog, StepStatus, SyncStep}

object SelectionAlgorithmBenchmark {

  type BenchmarkConfig = {
    def benchmarkDataDirectory: String // directory for the data stored for this benchmark
  }

  object CreateBenchmark extends SyncStep {

    sealed trait CreateBenchmarkNetworkFlows
    case object NoNetworkFlows extends CreateBenchmarkNetworkFlows
    case class GenerateRandomNetworkFlows(
      max: Int, min: Int = 0
    ) extends CreateBenchmarkNetworkFlows {
      val r = scala.util.Random
      require(min > -1, "CreateBenchmarkNetworkFlows.min must be 0 or greater.")
      def generateFlow: Int = r.nextInt(max - min) + min
    }

    type CreateBenchmarkConfig = BenchmarkConfig {
      def sourceAssetsDirectory: String // location of the network.xml file
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
            // load the network graph
            val networkXML = XML.loadFile(s"${config.sourceAssetsDirectory}/network.xml")
            config.networkFlows match {
              case NoNetworkFlows =>
                LocalGraphOps.readMATSimXML(networkXML, None, BPRCostFunctionType, config.timeWindow)
              case GenerateRandomNetworkFlows(max, min) =>
                val snapshot = {
                  // for edgeId in graph
                  //   updateEdge with generateFlow
                  // save snapshot file from modifiedGraph

                }
                LocalGraphOps.readMATSimXML(networkXML, Some(snapshot), BPRCostFunctionType, config.timeWindow)
            }

            // add flows if needed

          }
          // create a population for this single batch
          // scaffold a call to MKSPLocalDijkstrasService
          // find the alternate paths for the population
          // scaffold a call to SelectionLocalCombinatorialService
          // run the selection algorithm, which finds the true optimal of the set
          // set up a file, or files for these results (xml?) that contains
          //   the population generated (As xml?)
          //   the graph used
          //   snapshot file of the flow generated
          //   the expected cost value of the graph via this addition
          //   the cost delta resulting from the group selected
          //   the selected alternate path set

          // we want utilities that can be called again that are related to this
          // such as load these things, write these things

          val outputLog = Map(
            "fs.xml.thing" -> "path"
          )
          outputLog
        }
      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }
}
