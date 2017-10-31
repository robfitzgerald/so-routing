package cse.fitzgero.sorouting.experiments.steps

import java.nio.file.{Files, Paths}
import java.time.LocalTime

import scala.util.Try
import scala.xml.XML

import cse.fitzgero.sorouting.experiments.ops.{ExperimentFSOps, ExperimentStepOps, MATSimOps}
import cse.fitzgero.sorouting.model.population.LocalPopulationOps
import cse.fitzgero.sorouting.model.population.LocalPopulationOps.LocalPopulationConfig
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalGraphOps
import edu.ucdenver.fitzgero.lib.experiment._

/**
  * Experiment Steps related to Experiment scaffolding
  */
object ExperimentAssetGenerator {

  type PopulationGeneratorConfig = {
    def populationSize: Int
    def networkURI: String
    def departTime: LocalTime
    def endTime: Option[LocalTime]
    def timeDeviation: Option[LocalTime]
  }



  object SetupConfigDirectory extends SyncStep {
    val name: String = "Copy basic assets to the config directory of this set of experiments, unless they are already there"
    override type StepConfig = {
      def sourceAssetsDirectory: String
      def experimentConfigDirectory: String
    }

    override def apply(config: StepConfig, log: ExperimentGlobalLog = Map()): Option[(StepStatus, ExperimentStepLog)] = Some {
      val t: Try[Map[String, String]] =
        Try {
          XML.save(
            s"${config.experimentConfigDirectory}/config.xml",
            XML.loadFile(s"${config.sourceAssetsDirectory}/config.xml"),
            ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.ConfigDocType
          )
          XML.save(
            s"${config.experimentConfigDirectory}/network.xml",
            XML.loadFile(s"${config.sourceAssetsDirectory}/network.xml"),
            ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.NetworkDocType
          )
          Map("fs.xml.config" -> s"${config.experimentConfigDirectory}/config.xml",
            "fs.xml.network" -> s"${config.experimentConfigDirectory}/network.xml")
        }
      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }



  object SetupInstanceDirectory extends SyncStep {
    val name: String = "Import config and network files for MATSim, updating the URIs for dependencies of this config.xml file"
    override type StepConfig = {
      def experimentConfigDirectory: String
      def experimentInstanceDirectory: String
    }

    override def apply(config: StepConfig, log: ExperimentGlobalLog = Map()): Option[(StepStatus, ExperimentStepLog)] = Some {
      val t: Try[Map[String, String]] =
        Try {
          MATSimOps.importExperimentConfig(config.experimentConfigDirectory, config.experimentInstanceDirectory)
          Map()
        }
      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }



  object LoadStoredPopulation extends SyncStep {
    val name: String = "Load a stored Population for use in this experiment"
    override type StepConfig = {
      def loadStoredPopulationPath: String
      def experimentInstanceDirectory: String
    }

    /**
      * expects a stored population location and copies it into the experiment instance directory
      * @param config has a loadStoredPopulationPath field
      * @param log global log
      * @return
      */
    override def apply(config: StepConfig, log: ExperimentGlobalLog = Map()): Option[(StepStatus, ExperimentStepLog)] = Some {
      val t: Try[Map[String, String]] =
        Try({
          val networkXml: xml.Elem = XML.loadFile(config.loadStoredPopulationPath)
          val destinationPath: String = ExperimentFSOps.populationFileURI(config.experimentInstanceDirectory)
          XML.save(destinationPath, networkXml, ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.PopulationDocType)
          Map("fs.xml.population" -> destinationPath)
        })
      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }



  object RepeatedPopulation extends SyncStep {
    val name: String = "Generate Population for repeated use"
    override type StepConfig = PopulationGeneratorConfig {
      def experimentConfigDirectory: String
      def experimentInstanceDirectory: String
    }

    /**
      * repeats the population for an entire experiment. will generate if repeat isn't possible. looks in the config directory for one to copy in
      * @param config has experiment config directory
      * @param log
      * @return
      */
    override def apply(config: StepConfig, log: ExperimentGlobalLog = Map()): Option[(StepStatus, ExperimentStepLog)] = Some {
      val t: Try[Map[String, String]] =
        Try({
          val destinationPath = ExperimentFSOps.populationFileURI(config.experimentInstanceDirectory)
          // look for a previous instance in this experimentInstanceDirectory
          val previousInstance: Option[String] = ExperimentFSOps
            .findPreviousPopulation(config.experimentConfigDirectory)

          previousInstance match {
            case Some(previousPopulation: String) =>
              // if there is a previous instance directory, copy the previous population into this instance
//              val sourcePopPath = ExperimentFSOps.populationFileURI(sourceInstanceDirectory)
              Files.createDirectories(Paths.get(config.experimentInstanceDirectory))
              XML.save(
                destinationPath,
                XML.loadFile(previousPopulation),
                ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.PopulationDocType)
            case None =>
              // if there are no instance directories here, create a new population
              Files.createDirectories(Paths.get(config.experimentInstanceDirectory))
              XML.save(
                destinationPath,
                generatePopulation(config.populationSize, config.networkURI, config.departTime, config.timeDeviation),
                ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.PopulationDocType)
          }

          Map("fs.xml.population" -> destinationPath)
        })
      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }



  object UniquePopulation extends SyncStep {
    val name: String = "Generate unique Population on each call"
    override type StepConfig = PopulationGeneratorConfig {
      def experimentInstanceDirectory: String
    }

    override def apply(config: StepConfig, log: ExperimentGlobalLog = Map()): Option[(StepStatus, ExperimentStepLog)] = Some {
      // create a new population and store it in the instance directory
      val t: Try[Map[String, String]] =
        Try({
          val destinationPath = ExperimentFSOps.populationFileURI(config.experimentInstanceDirectory)
          Files.createDirectories(Paths.get(config.experimentInstanceDirectory))
          XML.save(
            destinationPath,
            generatePopulation(config.populationSize, config.networkURI, config.departTime, config.timeDeviation),
            ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.PopulationDocType)

          Map("fs.xml.population" -> destinationPath)
        })

      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }


  // TODO: move helper functions into a helper object (an Ops object)
  private[ExperimentAssetGenerator]
  def generatePopulation(popSize: Int, networkPath: String, departTime: LocalTime, timeDeviation: Option[LocalTime]): xml.Elem = {
    val networkXml: xml.Elem = XML.loadFile(networkPath)
    val graph = LocalGraphOps.readMATSimXML(networkXml)
    val populationConfig: LocalPopulationConfig = LocalPopulationConfig(popSize, departTime, timeDeviation)
    val requests = LocalPopulationOps.generateRequests(graph, populationConfig)
    LocalPopulationOps.generateXMLRequests(graph, requests)
  }
}

