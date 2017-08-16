package cse.fitzgero.sorouting.util

import java.nio.file.{Files, _}
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, LocalTime}

import scala.collection.JavaConverters._
import scala.xml.{Elem, XML}
import scala.util.matching.Regex
import scala.xml.dtd.{DocType, SystemID}
import cse.fitzgero.sorouting.matsimrunner.population.{Population, PopulationOneTrip}

// what characters are valid for all file systems?
// [0-9a-zA-Z-.,_]
// from https://superuser.com/questions/358855/what-characters-are-safe-in-cross-platform-file-names-for-linux-windows-and-os

// NEW!!!!
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

sealed trait SORoutingExperimentType
case object FullUEExp extends SORoutingExperimentType {
  override def toString: String = "full-ue"
}
case object CombinedUESOExp extends SORoutingExperimentType {
  override def toString: String = "combined-ue-so"
}

sealed trait SORoutingPopulationType
case object FullUEPopulation extends SORoutingPopulationType {
  override def toString: String = s"population-full-ue"
}
case object CombinedUESOPopulation extends SORoutingPopulationType {
  override def toString: String = s"population-combined-ue-so"
}
//case object PrefixPopulation extends SORoutingPopulationType {
//  override def toString: String = s"population-prefix"
//}
case object SnapshotPopulation extends SORoutingPopulationType {
  override def toString: String = s"population-snapshot"
}

class SORoutingFilesHelper(val conf: SORoutingApplicationConfig) {
  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH.mm.ss")
  private val baseDir: String = if (conf.workingDirectory.head == '/') conf.workingDirectory else s"${Paths.get("").toAbsolutePath.toString}/${conf.workingDirectory}"
  private val config: xml.Elem = XML.loadFile(conf.matsimConfigFile)
  val network: xml.Elem = XML.loadFile(conf.matsimNetworkFile)
  private val configHash: String = config.hashCode().toString

  private val experimentTime: String = LocalDateTime.now().toString

  val thisExperimentDirectory: String = s"$baseDir/$configHash/$experimentTime"
  val snapshotsBaseDirectory: String = s"$thisExperimentDirectory/snapshots"
  val resultsDirectory: String = s"$thisExperimentDirectory/results"
  //  val tempDirectory: String = s"$thisExperimentDirectory/temp"

  val fullUEResultsDirectory: String = s"$resultsDirectory/$FullUEExp"
  val combinedUESOResultsDirectory: String = s"$resultsDirectory/$CombinedUESOExp"
  val thisNetworkFilePath: String = s"$thisExperimentDirectory/network.xml"

  // MATSim XML DocTypes for writing new files
  val WriteXmlDeclaration = true
  val configDocType = DocType("config", SystemID("http://www.matsim.org/files/dtd/config_v1.dtd"), Nil)
  val networkDocType = DocType("network", SystemID("http://www.matsim.org/files/dtd/network_v1.dtd"), Nil)
  val populationDocType = DocType("population", SystemID("http://www.matsim.org/files/dtd/population_v6.dtd"), Nil)

//  lazy val configDirectoryExists: Boolean =
//    Files.isDirectory(Paths.get(thisExperimentDirectory))
//
//  lazy val assetsToConstruct: Set[String] =
//    SORoutingFilesHelper.scaffolding.filter(uri => {
//      !Files.exists(Paths.get(s"$thisExperimentDirectory/$uri"))
//    })

  println(s"Scaffolding experiment assets in directory $thisExperimentDirectory")
  scaffoldFileRequirements().foreach(println)


  private def scaffoldFileRequirements(): Set[String] = {
    val confWithNetwork = updateFileNameIn("network", config, thisNetworkFilePath)
    Set(
      // directories
      Files.createDirectories(Paths.get(thisExperimentDirectory)).toString,
      Files.createDirectories(Paths.get(snapshotsBaseDirectory)).toString,
      Files.createDirectories(Paths.get(fullUEResultsDirectory)).toString,
      Files.createDirectories(Paths.get(combinedUESOResultsDirectory)).toString,
      makeConfigXml(s"config-$FullUEExp.xml", updateFileNameIn("plans", confWithNetwork, finalPopulationFilePath(FullUEExp))),
//      makeConfigXml(s"config-$CombinedUESOExp.xml", updateFileNameIn("plans", confWithNetwork, populationFilePath(CombinedUESOExp))),
      makeNetworkXml(s"network.xml", network)
    )
  }


  /**
    * set up the directory for this snapshot run
    * @param population the population which we wish to use in this MATSim snapshot run
    * @param timeGroup start time of this timeGroup, which is also the end time of this MATSim Snapshot run
    * @return
    */
  def scaffoldSnapshot(population: PopulationOneTrip, timeGroup: LocalTime): String = {
    // creates a directory with the population and config files
    val thisSnapGroup = timeGroup.format(HHmmssFormat)
    val thisSnapDir = s"$snapshotsBaseDirectory/matsim-snapshot-run-$thisSnapGroup"
    val matsimOutputDir = s"$thisSnapDir/matsim-output"
    val popFilePath = s"$thisSnapDir/population-snapshot.xml"
    val networkFilePath = s"$thisSnapDir/network-snapshot.xml"

    Files.createDirectories(Paths.get(thisSnapDir)).toString
    Files.createDirectories(Paths.get(matsimOutputDir)).toString

    val configSnapshot = updateFileNameIn("network", updateFileNameIn("plans", config, popFilePath), networkFilePath)

    makeXmlFile(thisSnapDir, configDocType)("config-snapshot.xml", configSnapshot)
    makeXmlFile(thisSnapDir, networkDocType)("network-snapshot.xml", network)
    makeXmlFile(thisSnapDir, populationDocType)("population-snapshot.xml", population.toXml)

    thisSnapDir
  }

//  /**
//    * writes generated population data to the correct file location
//    * @param elem a population file
//    * @param expType denotes the type of experiment associated with this population
//    * @return
//    */
//  def writePopulationFile(elem: xml.Elem, expType: SORoutingExperimentType): String = {
//    val filePath = finalPopulationFilePath(expType)
//    XML.save(filePath, elem)
//    filePath
//  }

  /**
    * gives the directory for the results of a given experiment type for the current experiment
    * @param expType  denotes the type of experiment associated with these results
    * @return
    */
  def experimentPath(expType: SORoutingExperimentType): String =
    s"$resultsDirectory/$expType"

  def finalConfigFilePath(expType: SORoutingExperimentType): String =
    s"$thisExperimentDirectory/config-$expType.xml"

  def finalPopulationFilePath(expType: SORoutingExperimentType): String =
    s"$thisExperimentDirectory/population-$expType.xml"

//  def tempPopulationFilePath(popType: SORoutingPopulationType): String =
//    s"$tempDirectory/$popType"
//
//  def matsimSnapshotRunPath(time: LocalTime): String =
//    s"$tempDirectory/matsim-snapshot-run-${time.format(DateTimeFormatter.ofPattern("HH.mm.ss"))}"

  /**
    * finds the last enumerated results directory. see MATSim > output for example.
    * @param dir the directory where results are found, a directory of enumerated directories beginning at 00
    * @return the name of the last enumerated directory
    */
  def getLastIterationDirectory(dir: String): String = {
    val paths: Iterator[Path] = Files.list(Paths.get(dir)).iterator.asScala
    paths.map(_.getFileName).toArray.map(_.toString.toInt).max.toString
  }


//  private val snapshotTimeParse: Regex = ".*snapshot-([0-9]{2}.[0-9]{2}.[0-9]{2}).*".r
//  /**
//    * parses the LocalTime value from the naming of a snapshot file
//    * @param s file name to be tested against the snapshot file pattern
//    * @return time associated with the snapshot, or LocalTime.MIDNIGHT if failed to match
//    */
//  def parseSnapshotForTime(s: String): LocalTime = s match {
//    case snapshotTimeParse(time) => LocalTime.parse(time.map(ch=>if (ch == '.') ':' else ch))
//    case _ => LocalTime.MIDNIGHT
//  }


  /**
    * dives into a MATSim config.xml file and alters all values it finds within a <param name="" value=""/> tag (should be one)
    * @param moduleName should likely be "plans" or "network"
    * @param configFile MATSim config.xml file
    * @param newFilePath substitute parameter value for the input file of this module
    * @return
    */
  private def updateFileNameIn(moduleName: String, configFile: xml.Elem, newFilePath: String): xml.Elem = {
    val plans = configFile \ "module" filter (_.attribute("name").head.text == moduleName)
    val currentConfigFilename = (plans \ "param" \ "@value").text
    XML.loadString(configFile.toString.replace(currentConfigFilename, newFilePath))
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

  def makeConfigXml(fileName: String, elem: xml.Elem): String = makeXmlFile(thisExperimentDirectory, configDocType)(fileName, elem)
  def makeNetworkXml(fileName: String, elem: xml.Elem): String = makeXmlFile(thisExperimentDirectory, networkDocType)(fileName, elem)
//  def makePopulationXml()(fileName: String, elem: xml.Elem): String = makeXmlFile(thisExperimentDirectory, populationDocType)(fileName, elem)
  def savePopulation(pop: PopulationOneTrip, expType: SORoutingExperimentType, popType: SORoutingPopulationType): Unit =
    popType match {
      case FullUEPopulation =>
        XML.save(finalPopulationFilePath(expType), pop.toXml, "UTF-8", WriteXmlDeclaration, populationDocType)
      case CombinedUESOPopulation =>
        XML.save(finalPopulationFilePath(expType), pop.toXml, "UTF-8", WriteXmlDeclaration, populationDocType)
    }

}

object SORoutingFilesHelper {
  def apply(config: SORoutingApplicationConfig): SORoutingFilesHelper =
    new SORoutingFilesHelper(config)
}