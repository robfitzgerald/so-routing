//package cse.fitzgero.sorouting.util
//
//import java.nio.file.{Files, _}
//import java.time.{LocalDateTime, LocalTime}
//import scala.collection.JavaConverters._
//import scala.xml.{Elem, XML}
//import scala.util.matching.Regex
//import scala.xml.dtd.{DocType, SystemID}
//
//import cse.fitzgero.sorouting.matsimrunner.population.Population
//
////
//// $WORKING_DIR - basic scaffolding
////   <configHash>/
////     config-full-ue.xml
////     config-partial-ue.xml
////     config-combined-ue-so.xml
////     snapshot/
////     experiments/
////       <time>/
////         results/
////           full-ue/
////           partial-ue/
////           combined-ue-so/
////
//
////
//// $WORKING_DIR - after experiment (with pop files and full directories)
////   <configHash>/
////     config-full-ue.xml
////     config-partial-ue.xml
////     config-combined-ue-so.xml
////     snapshot/ ..
////     experiments/
////       <time>/
////         population-generated.xml
////         population-partial-ue.xml
////         population-combined-ue-so.xml
////         results/
////           full-ue/ ..
////           partial-ue/ ..
////           combined-ue-so/ ..
////
//
//sealed trait SORoutingExperimentType
//case object FullUEExp extends SORoutingExperimentType {
//  override def toString: String = "full-ue"
//}
//case object PartialUEExp extends SORoutingExperimentType {
//  override def toString: String = "partial-ue"
//}
//case object CombinedUESOExp extends SORoutingExperimentType {
//  override def toString: String = "combined-ue-so"
//}
//
//class SORoutingFilesHelper(private val configFileName: String, private val networkFileName: String, private val workingDirectoryParam: String) {
//  private val baseDir: String = if (workingDirectoryParam.head == '/') workingDirectoryParam else s"${Paths.get("").toAbsolutePath.toString}/$workingDirectoryParam"
//  private val config: xml.Elem = XML.loadFile(configFileName)
//  val network: xml.Elem = XML.loadFile(networkFileName)
//  private val configHash: String = config.hashCode().toString
//  private val experimentTime: String = LocalDateTime.now().toString
//
//  val thisConfigDirectory: String = s"$baseDir/$configHash"
//  val thisNetworkFilePath: String = s"$thisConfigDirectory/network.xml"
//  val snapshotDirectory: String = s"$baseDir/$configHash/snapshot"
//  val experimentDirectory: String = s"$baseDir/$configHash/experiments/$experimentTime"
//  val fullUEResultsDirectory: String = s"$experimentDirectory/results/$FullUEExp"
//  val partialUEResultsDirectory: String = s"$experimentDirectory/results/$PartialUEExp"
//  val combinedUESOResultsDirectory: String = s"$experimentDirectory/results/$CombinedUESOExp"
//
//  // MATSim XML DocTypes for writing new files
//  val WriteXmlDeclaration = true
//  val configDocType = DocType("config", SystemID("http://www.matsim.org/files/dtd/config_v1.dtd"), Nil)
//  val networkDocType = DocType("network", SystemID("http://www.matsim.org/files/dtd/network_v1.dtd"), Nil)
//  val populationDocType = DocType("population", SystemID("http://www.matsim.org/files/dtd/population_v6.dtd"), Nil)
//
//  lazy val configDirectoryExists: Boolean =
//    Files.isDirectory(Paths.get(thisConfigDirectory))
//  lazy val assetsToConstruct: Set[String] =
//    SORoutingFilesHelper.scaffolding.filter(uri => {
//      !Files.exists(Paths.get(s"$thisConfigDirectory/$uri"))
//    })
//
//  println(s"Scaffolding experiment assets in directory $thisConfigDirectory")
//  scaffoldFileRequirements().foreach(println)
//
//  private def scaffoldFileRequirements(): Set[String] = {
//    if (configDirectoryExists) {
//      if (assetsToConstruct.nonEmpty) throw new FileAlreadyExistsException(s"config hashed directory $configHash exists but missing some files: $assetsToConstruct")
//      else {
//        Set(Files.createDirectory(Paths.get(experimentDirectory)).toString)
//      }
//    } else {
//      // make everything
//      val confWithNetwork = updateFileNameIn("network", config, thisNetworkFilePath)
//      Set(
//        Files.createDirectories(Paths.get(thisConfigDirectory)).toString,
//        Files.createDirectories(Paths.get(snapshotDirectory)).toString,
//        Files.createDirectories(Paths.get(experimentDirectory)).toString,
//        Files.createDirectories(Paths.get(fullUEResultsDirectory)).toString,
//        Files.createDirectories(Paths.get(partialUEResultsDirectory)).toString,
//        Files.createDirectories(Paths.get(combinedUESOResultsDirectory)).toString,
//        makeConfigXml(s"config-$FullUEExp.xml", updateFileNameIn("plans", confWithNetwork, populationFilePath(FullUEExp))),
//        makeConfigXml(s"config-$PartialUEExp.xml", updateFileNameIn("plans", confWithNetwork, populationFilePath(PartialUEExp))),
//        makeConfigXml(s"config-$CombinedUESOExp.xml", updateFileNameIn("plans", confWithNetwork, populationFilePath(CombinedUESOExp))),
//        makeNetworkXml(s"network.xml", network)
//      )
//    }
//  }
//
//  /**
//    * writes generated population data to the correct file location
//    * @param elem a population file
//    * @param expType denotes the type of experiment associated with this population
//    * @return
//    */
//  def writePopulationFile(elem: xml.Elem, expType: SORoutingExperimentType): String = {
//    val filePath = populationFilePath(expType)
//    XML.save(filePath, elem)
//    filePath
//  }
//
//  /**
//    * gives the directory for the results of a given experiment type for the current experiment
//    * @param expType  denotes the type of experiment associated with these results
//    * @return
//    */
//  def experimentPath(expType: SORoutingExperimentType): String =
//    s"$experimentDirectory/results/$expType"
//
//  def configFilePath(expType: SORoutingExperimentType): String =
//    s"$thisConfigDirectory/config-$expType.xml"
//
//  def populationFilePath(expType: SORoutingExperimentType): String =
//    s"$experimentDirectory/population-$expType.xml"
//
//  def snapshotFileList: Seq[String] = {
//    // @TODO: move these snapshots down two directories so they can be shared and not repeated
//    val snapshotDirectory = s"${experimentPath(PartialUEExp)}/snapshot"
//    println(s"getting snapshotDirectory file list via this path:")
//    println(s"$snapshotDirectory")
//    val lastIteration = getLastIterationDirectory(snapshotDirectory)
//    val snapshotLastIterationDirectory = s"$snapshotDirectory/$lastIteration"
//    println(snapshotLastIterationDirectory)
//    Files.list(Paths.get(snapshotLastIterationDirectory)).toArray.map(_.toString)
//    //    Files.list(Paths.get(snapshotDirectory)).toArray.map(_.toString) - original
//  }
//
//  //
//  def getLastIterationDirectory(dir: String): String = {
//    val paths: Iterator[Path] = Files.list(Paths.get(dir)).iterator.asScala
//    paths.map(_.getFileName).toArray.map(_.toString.toInt).max.toString
//  }
//
//
//  private val snapshotTimeParse: Regex = ".*snapshot-([0-9]{2}.[0-9]{2}.[0-9]{2}).*".r
//  def parseSnapshotForTime(s: String): LocalTime = s match {
//    case snapshotTimeParse(time) => LocalTime.parse(time.map(ch=>if (ch == '.') ':' else ch))
//  }
//
//
//  /**
//    * dives into a MATSim config.xml file and alters all values it finds within a <param name="" value=""/> tag (should be one)
//    * @param moduleName should likely be "plans" or "network"
//    * @param configFile MATSim config.xml file
//    * @param newFilePath substitute parameter value for the input file of this module
//    * @return
//    */
//  private def updateFileNameIn(moduleName: String, configFile: xml.Elem, newFilePath: String): xml.Elem = {
//    val plans = configFile \ "module" filter (_.attribute("name").head.text == moduleName)
//    val currentConfigFilename = (plans \ "param" \ "@value").text
//    XML.loadString(configFile.toString.replace(currentConfigFilename, newFilePath))
//  }
//
//
//
//  /**
//    * given a local filename and an xml element, write this file to the working directory
//    * @param fileName the file name with extension
//    * @param elem an xml.Elem object we want to write
//    * @return
//    */
//  private def makeXmlFile(dir: String, docType: DocType)(fileName: String, elem: xml.Elem): String = {
//    val fileDestination = s"$dir/$fileName"
//    XML.save(fileDestination, elem, "UTF-8", WriteXmlDeclaration, docType)
//    fileDestination
//  }
//  def makeConfigXml(fileName: String, elem: xml.Elem): String = makeXmlFile(thisConfigDirectory, configDocType)(fileName, elem)
//  def makeNetworkXml(fileName: String, elem: xml.Elem): String = makeXmlFile(thisConfigDirectory, networkDocType)(fileName, elem)
//  def makePopulationXml()(fileName: String, elem: xml.Elem): String = makeXmlFile(experimentDirectory, populationDocType)(fileName, elem)
//  def savePopulation(pop: Population, expType: SORoutingExperimentType): Unit =
//    XML.save(populationFilePath(expType), pop.toXml, "UTF-8", WriteXmlDeclaration, populationDocType)
//
//}
//
//object SORoutingFilesHelper {
//  // list of names which should be found in the experiment directory
//  def scaffolding: Set[String] =
//    Set(
//      "config-full-ue.xml",
//      "config-partial-ue.xml",
//      "config-combined-ue-so.xml",
//      "snapshot",
//      "experiments"
//    )
//  def apply(config: SORoutingApplicationConfig): SORoutingFilesHelper =
//    new SORoutingFilesHelper(config.matsimConfigFile, config.matsimNetworkFile, config.workingDirectory)
//}