package cse.fitzgero.sorouting.util

import java.nio.file.{Files, _}
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, LocalTime}
import scala.collection.JavaConverters._
import scala.xml.XML
import scala.xml.dtd.{DocType, SystemID}
import scala.util.{Failure, Success, Try}

import cse.fitzgero.sorouting.matsimrunner.population.PopulationOneTrip


/**
  * Produces a directory layout (See bottom of SORoutingFileHelper.scala) and provides utilities for fs interaction within the experiment
  * @param conf configuration of this experiment
  */
class SORoutingFilesHelper(val conf: SORoutingApplicationConfig) {

  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Constructor
  ////////////////////////////////////////////////////////////////////////////////////////////////////

  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm.ss")

  // private xml and config assets
  private val config: xml.Elem = XML.loadFile(conf.matsimConfigFile)
  private val network: xml.Elem = XML.loadFile(conf.matsimNetworkFile)
  private val configHash: String = config.hashCode().toString
  private val experimentTime: String = LocalDateTime.now().toString


  // Directory information for this experiment
  private val baseDir: String = if (conf.workingDirectory.head == '/') conf.workingDirectory else s"${Paths.get("").toAbsolutePath.toString}/${conf.workingDirectory}"
  val thisExperimentDirectory: String = s"$baseDir/$configHash/$experimentTime"
  private val snapshotsBaseDirectory: String = s"$thisExperimentDirectory/snapshots"
  private val resultsDirectory: String = s"$thisExperimentDirectory/results"
  private val fullUEResultsDirectory: String = s"$resultsDirectory/$FullUEExp"
  private val combinedUESOResultsDirectory: String = s"$resultsDirectory/$CombinedUESOExp"
  val thisNetworkFilePath: String = s"$thisExperimentDirectory/network.xml"


  // MATSim XML DocTypes for writing new files
  private val WriteXmlDeclaration = true
  private val configDocType = DocType("config", SystemID("http://www.matsim.org/files/dtd/config_v1.dtd"), Nil)
  private val networkDocType = DocType("network", SystemID("http://www.matsim.org/files/dtd/network_v1.dtd"), Nil)
  private val populationDocType = DocType("population", SystemID("http://www.matsim.org/files/dtd/population_v6.dtd"), Nil)


  // build the basic file directory setup for this experiment
  scaffoldFileRequirements()



  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Public methods
  ////////////////////////////////////////////////////////////////////////////////////////////////////


  def getNetwork: xml.Elem = network


  /**
    * set up the directory for this snapshot run
    * @param population the population which we wish to use in this MATSim snapshot run
    * @param timeGroupStart start time of this timeGroup, which is also the end time of this MATSim Snapshot run
    * @param timeGroupEnd end time for this timegroup
    * @return
    */
  def scaffoldSnapshot(population: PopulationOneTrip, timeGroupStart: LocalTime, timeGroupEnd: LocalTime): String = {
    // creates a directory with the population and config files
    val thisSnapGroup = timeGroupStart.format(HHmmssFormat)
    val thisSnapDir = s"$snapshotsBaseDirectory/matsim-snapshot-run-$thisSnapGroup"
    val matsimOutputDir = s"$thisSnapDir/matsim-output"
    val popFilePath = s"$thisSnapDir/population-snapshot.xml"
    val networkFilePath = s"$thisSnapDir/network-snapshot.xml"

    Files.createDirectories(Paths.get(thisSnapDir)).toString
    Files.createDirectories(Paths.get(matsimOutputDir)).toString

    // TODO: pass timeGroupStart and timeGroupEnd into config file

    val configSnapshot = modifyModuleValue("network", modifyModuleValue("plans", config, popFilePath), networkFilePath)

    makeXmlFile(thisSnapDir, configDocType)("config-snapshot.xml", configSnapshot)
    makeXmlFile(thisSnapDir, networkDocType)("network-snapshot.xml", network)
    makeXmlFile(thisSnapDir, populationDocType)("population-snapshot.xml", population.toXml)

    thisSnapDir
  }


  /**
    * utility function for saving population files in their correct file path
    * @param pop the population to be saved
    * @param expType the phase of the experiment
    * @param popType the population type
    */
  def savePopulation(pop: PopulationOneTrip, expType: SORoutingExperimentType, popType: SORoutingPopulationType): Unit =
    popType match {
      case FullUEPopulation =>
        XML.save(finalPopulationFilePath(expType), pop.toXml, "UTF-8", WriteXmlDeclaration, populationDocType)
      case CombinedUESOPopulation =>
        XML.save(finalPopulationFilePath(expType), pop.toXml, "UTF-8", WriteXmlDeclaration, populationDocType)
    }


  /**
    * gives the directory for the results of a given experiment type for the current experiment
    * @param expType  denotes the type of experiment associated with these results
    * @return
    */
  def experimentPath(expType: SORoutingExperimentType): String =
    s"$resultsDirectory/$expType"


  /**
    * the path for the config files at the base directory of this experiment (the final versions)
    * @param expType set the type of experiment for this config file
    * @return
    */
  def finalConfigFilePath(expType: SORoutingExperimentType): String =
    s"$thisExperimentDirectory/config-$expType.xml"

  /**
    * the path for the population files at the base directory of this experiment (the final versions)
    * @param expType set the type of experiment for this config file
    * @return
    */
  def finalPopulationFilePath(expType: SORoutingExperimentType): String =
    s"$thisExperimentDirectory/population-$expType.xml"




  ////////////////////////////////////////////////////////////////////////////////////////////////////
  // Private Methods
  ////////////////////////////////////////////////////////////////////////////////////////////////////


  /**
    * constructs the base directories and shared files for this experiment
    * @return the list of files and folders created
    */
  private def scaffoldFileRequirements(): Set[String] = {
    val confWithNetwork = modifyModuleValue("network", config, thisNetworkFilePath)
    println(confWithNetwork.toString)
    Set(
    // directories
    Files.createDirectories(Paths.get(thisExperimentDirectory)).toString,
    Files.createDirectories(Paths.get(snapshotsBaseDirectory)).toString,
    Files.createDirectories(Paths.get(fullUEResultsDirectory)).toString,
    Files.createDirectories(Paths.get(combinedUESOResultsDirectory)).toString,
    makeNetworkXml(s"network.xml", network),
    makeConfigXml(s"config-$FullUEExp.xml", modifyModuleValue("plans", confWithNetwork, finalPopulationFilePath(FullUEExp))),
    makeConfigXml(s"config-$CombinedUESOExp.xml", modifyModuleValue("plans", confWithNetwork, finalPopulationFilePath(CombinedUESOExp)))
    )
  }


  /**
    * finds the last enumerated results directory. see MATSim > output for example.
    * @param dir the directory where results are found, a directory of enumerated directories beginning at 00
    * @return the name of the last enumerated directory
    */
  private def getLastIterationDirectory(dir: String): String = {
    val paths: Iterator[Path] = Files.list(Paths.get(dir)).iterator.asScala
    paths.map(_.getFileName).toArray.map(_.toString.toInt).max.toString
  }


  /**
    * dives into a MATSim config.xml file and alters all values it finds within a <param name="" value=""/> tag (should be one)
    * @param moduleName name of a module in the config file
    * @param configFile MATSim config.xml file
    * @param newFilePath substitute parameter value for the input file of this module
    * @return
    */
  private def modifyModuleValue(moduleName: String, configFile: xml.Elem, newFilePath: String): xml.Elem = {
    val plans = configFile \ "module" filter (_.attribute("name").head.text == moduleName)
    val currentValue = (plans \ "param" \ "@value").text
    if (currentValue.isEmpty) throw new IllegalArgumentException(s"due to the design of Scala's XML library, updates to XML properties is performed by string replacement. The $moduleName value was found to be the empty string, which cannot be used for string replacement.")
    val updated: String = configFile.toString.replace(currentValue, newFilePath)
    Try({XML.loadString(updated)}) match {
      case Success(xml) => xml
      case Failure(e) => throw new IllegalArgumentException(s"XML file deserialization failed when modifying value $currentValue at key $moduleName: ${e.getMessage}")
    }
  }


  /**
    * given a local filename and an xml element, write this file to the working directory
    * @param fileName the file name with extension
    * @param elem an xml.Elem object we want to write
    * @return
    */
  private def makeXmlFile(dir: String, docType: DocType)(fileName: String, elem: xml.Elem): String = {
    val fileDestination = s"$dir/$fileName"
    XML.save(fileDestination, elem, "UTF-8", WriteXmlDeclaration, docType)
    fileDestination
  }

  private def makeConfigXml(fileName: String, elem: xml.Elem): String = makeXmlFile(thisExperimentDirectory, configDocType)(fileName, elem)
  private def makeNetworkXml(fileName: String, elem: xml.Elem): String = makeXmlFile(thisExperimentDirectory, networkDocType)(fileName, elem)

}

object SORoutingFilesHelper {
  def apply(config: SORoutingApplicationConfig): SORoutingFilesHelper =
    new SORoutingFilesHelper(config)
}


// Directory Layout
//
// $WORKING_DIR
// <config-name>             <- replace with meaningful naming scheme (configHash property)
//   <timestamp>
//     config-full-ue.xml                      the original matsim config file
//     config-combined-ue-so.xml               the config which points to the final all-populations plan
//     network.xml                             the original matsim network file
//     population-full-ue.xml                  population generated by experiment runner with no SO routes
//     population-combined-ue-so.xml           final population generated after running all snapshots
//     snapshots/
//       *matsim-snapshot-run-<time>/
//         *population-snapshot.xml
//         *config-snapshot.xml
//         *network-snapshot.xml
//         *matsim-output/
//     results/
//       full-ue/
//         <matsim output>
//       combined-ue-so/
//         <matsim output>


// what characters are valid for all file systems?
// [0-9a-zA-Z-.,_]
// from https://superuser.com/questions/358855/what-characters-are-safe-in-cross-platform-file-names-for-linux-windows-and-os
