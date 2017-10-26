package cse.fitzgero.sorouting.experiments.steps

import java.nio.file.{Files, Paths}
import java.time.LocalTime

import cse.fitzgero.sorouting.experiments.ops.{ExperimentFSOps, ExperimentStepOps}
import cse.fitzgero.sorouting.model.population.LocalPopulationOps
import cse.fitzgero.sorouting.model.population.LocalPopulationOps.LocalPopulationConfig
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalGraphOps
import edu.ucdenver.fitzgero.lib.experiment._

import scala.util.{Failure, Success, Try}
import scala.xml.XML

object ExperimentAssetGenerator {

  type PopulationGeneratorConfig = {
    def populationSize: Int
    def networkURI: String
    def startTime: LocalTime
    def endTime: Option[LocalTime]
  }

  type DirectoriesConfig = {
    def experimentSetDirectory: String // sits above all configurations in a set of related tests
    def experimentConfigDirectory: String // has the base config and a set of instance directories
    def experimentInstanceDirectory: String // a date/time-named directory
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
          importExperimentConfig(config.experimentConfigDirectory, config.experimentInstanceDirectory)
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



  object Repeated extends SyncStep {
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
            .findDateTimeStrings(config.experimentConfigDirectory).sorted.lastOption

          previousInstance match {
            case Some(sourceInstanceDirectory: String) =>
              // if there is a previous instance directory, copy the previous population into this instance
              val sourcePopPath = ExperimentFSOps.populationFileURI(sourceInstanceDirectory)
              Files.createDirectories(Paths.get(config.experimentInstanceDirectory))
              XML.save(
                destinationPath,
                XML.loadFile(sourcePopPath),
                ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.PopulationDocType)
            case None =>
              // if there are no instance directories here, create a new population
              Files.createDirectories(Paths.get(config.experimentInstanceDirectory))
              XML.save(
                destinationPath,
                generatePopulation(config.populationSize, config.networkURI, config.startTime, config.endTime),
                ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.PopulationDocType)
          }

          Map("fs.xml.population" -> destinationPath)
        })
      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }



  object Unique extends SyncStep {
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
            generatePopulation(config.populationSize, config.networkURI, config.startTime, config.endTime),
            ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.PopulationDocType)

          Map("fs.xml.population" -> destinationPath)
        })

      ExperimentStepOps.resolveTry(t, Some(name))
    }
  }



  private[ExperimentAssetGenerator]
  def generatePopulation(popSize: Int, networkPath: String, startTime: LocalTime, endTime: Option[LocalTime]): xml.Elem = {
    val networkXml: xml.Elem = XML.loadFile(networkPath)
    val graph = LocalGraphOps.readMATSimXML(networkXml)
    val populationConfig: LocalPopulationConfig = LocalPopulationConfig(popSize, startTime, endTime)
    val requests = LocalPopulationOps.generateRequests(graph, populationConfig)
    LocalPopulationOps.generateXMLRequests(graph, requests)
  }


  private[ExperimentAssetGenerator]
  def importExperimentConfig(experimentConfigDirectory: String, experimentInstanceDirectory: String): Unit = {
    val thisInstanceAbsolutePath: String = Paths.get(experimentInstanceDirectory).toAbsolutePath.toString
    val thisNetworkURI = s"$thisInstanceAbsolutePath/network.xml"
    val thisPopulationURI = s"$thisInstanceAbsolutePath/population.xml"
    Try {

      val updatedConfigFile: xml.Elem =
        List(
          ("network", thisNetworkURI),
          ("plans", thisPopulationURI)
        ).foldLeft(XML.loadFile(s"$experimentConfigDirectory/config.xml"))((xml, updateData) =>
          modifyModuleValue(xml, updateData._1, updateData._2)
        )

      Files.createDirectories(Paths.get(experimentInstanceDirectory))

      XML.save(
        s"$experimentInstanceDirectory/config.xml",
        updatedConfigFile,
        ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.ConfigDocType
      )
      XML.save(
        s"$experimentInstanceDirectory/network.xml",
        XML.loadFile(s"$experimentConfigDirectory/network.xml"),
        ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, ExperimentFSOps.NetworkDocType
      )
    }
    // unhandled Try block
  }

  /**
    * dives into a MATSim config.xml file and alters all values it finds within a <param name="" value=""/> tag (should be one)
    * @param matsimConfig MATSim config.xml file
    * @param moduleName name of a module in the config file
    * @param newFilePath substitute parameter value for the input file of this module
    * @return
    */
  private[ExperimentAssetGenerator] def modifyModuleValue(matsimConfig: xml.Elem, moduleName: String, newFilePath: String): xml.Elem = {
    val plans = matsimConfig \ "module" filter (_.attribute("name").head.text == moduleName)
    val currentValue = (plans \ "param" \ "@value").text
    if (currentValue.isEmpty) throw new IllegalArgumentException(s"due to the design of Scala's XML library, updates to XML properties is performed by string replacement. The $moduleName value was found to be the empty string, which cannot be used for string replacement.")
    val updated: String = matsimConfig.toString.replace(currentValue, newFilePath)
    Try({XML.loadString(updated)}) match {
      case Success(xml) => xml
      case Failure(e) => throw new IllegalArgumentException(s"XML file deserialization failed when modifying value $currentValue at key $moduleName: ${e.getMessage}")
    }
  }
}

