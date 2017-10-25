package cse.fitzgero.sorouting.experiments.steps

import java.time.LocalTime

import cse.fitzgero.sorouting.experiments.{ExperimentFSOps, ExperimentStepOps}
import cse.fitzgero.sorouting.model.population.LocalPopulationOps
import cse.fitzgero.sorouting.model.population.LocalPopulationOps.LocalPopulationConfig
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalGraphOps
import edu.ucdenver.fitzgero.lib.experiment._

import scala.util.Try
import scala.xml.XML

object PopulationGenerator {

  type PopulationGeneratorConfig = {
    def populationSize: Int
    def networkPath: String
    def startTime: LocalTime
    def endTime: Option[LocalTime]
  }

  type PopulationDirectoriesConfig = {
    def experimentSetDirectory: String // sits above all configurations in a set of related tests
    def experimentConfigDirectory: String // has the base config and a set of instance directories
    def experimentInstanceDirectory: String // a date/time-named directory
  }



  object LoadStoredPopulation extends SyncStep {
    val name: String = "Load a stored Population for use in this experiment"
    override type StepConfig = PopulationDirectoriesConfig {
      def loadStoredPopulationPath: String
    }

    /**
      * expects a stored population location and copies it into the experiment instance directory
      * @param config has a loadStoredPopulationPath field
      * @param log global log
      * @return
      */
    override def apply(config: StepConfig, log: ExperimentGlobalLog): Option[(StepStatus, ExperimentStepLog)] = Some {
      val t: Try[Map[String, String]] =
        Try({
          val networkXml: xml.Elem = XML.loadFile(config.loadStoredPopulationPath)
          val destinationPath: String = ExperimentFSOps.populationFileURI(config.experimentInstanceDirectory)
          XML.save(destinationPath, networkXml, "UTF-8", ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.PopulationDocType)
          Map("fs.xml.population" -> destinationPath)
        })
      ExperimentStepOps.resolveTry(t)
    }
  }



  object Repeated extends SyncStep {
    val name: String = "Generate Population for repeated use"
    override type StepConfig = PopulationGeneratorConfig with PopulationDirectoriesConfig

    /**
      * repeats the population for an entire experiment. will generate if repeat isn't possible. looks in the config directory for one to copy in
      * @param config has experiment config directory
      * @param log
      * @return
      */
    override def apply(config: StepConfig, log: ExperimentGlobalLog): Option[(StepStatus, ExperimentStepLog)] = {
      val t: Try[Map[String, String]] =
        Try({
          val popFilePath: String = ExperimentFSOps
            .findDateTimeStrings(config.experimentInstanceDirectory)
            .sorted
            .lastOption match {
            case Some(instanceDirectory) =>
              // if there are instance directories, copy the previous population into this instance
              ""
            case None =>
              // if there are no instance directories here, create a new population
              ""
          }

          // either way, log the location of it.
          Map()
        })
      ExperimentStepOps.resolveTry(t)
    }
  }



  object Unique extends SyncStep {
    val name: String = "Generate unique Population on each call"
    override type StepConfig = PopulationGeneratorConfig with PopulationDirectoriesConfig

    override def apply(config: StepConfig, log: ExperimentGlobalLog): Option[(StepStatus, ExperimentStepLog)] = {
      // create a new population and store it in the instance directory

      // log the location of it
    }
  }



  def generatePopulation(popSize: Int, networkPath: String, startTime: LocalTime, endTime: Option[LocalTime]): xml.Elem = {
    val networkXml: xml.Elem = XML.loadFile(networkPath)
    val graph = LocalGraphOps.readMATSimXML(networkXml)
    val populationConfig: LocalPopulationConfig = LocalPopulationConfig(popSize, startTime, endTime))
    val requests = LocalPopulationOps.generateRequests(graph, populationConfig)
    LocalPopulationOps.generateXMLRequests(graph, requests)
  }
}

