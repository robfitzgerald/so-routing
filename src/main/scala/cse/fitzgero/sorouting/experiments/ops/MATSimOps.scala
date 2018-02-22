package cse.fitzgero.sorouting.experiments.ops

import java.nio.file.{Files, Paths}
import java.time.LocalTime

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}
import scala.xml.XML

import cse.fitzgero.sorouting.matsimrunner.network.MATSimNetworkToCollection
import cse.fitzgero.sorouting.matsimrunner.snapshot.NewNetworkAnalyticStateCollector.SnapshotCollector
import cse.fitzgero.sorouting.matsimrunner.snapshot._
import cse.fitzgero.sorouting.model.roadnetwork.costfunction.BPRCostFunctionType
import cse.fitzgero.sorouting.model.roadnetwork.local.LocalGraphOps.EdgesWithDrivers
import cse.fitzgero.sorouting.model.roadnetwork.local.{LocalEdgeSimulationAttribute, LocalGraph, LocalGraphOps}
import org.matsim.api.core.v01.Scenario
import org.matsim.core.config.{Config, ConfigUtils}
import org.matsim.core.controler.{AbstractModule, Controler}
import org.matsim.core.scenario.ScenarioUtils

object MATSimOps {
  /**
    * runs a MATSim simulation, generating a single snapshot file at termination
    * @param currentDirectory base directory of this experiment instance, which should be populated with the config, network, and population assets
    * @param startTime the start of day in simulation time
    * @param endTime the time in simulation time to end, which defaults to the value LocalTime.MAX
    * @return
    */
  def MATSimRun (currentDirectory: String, startTime: LocalTime, endTime: LocalTime, timeWindow: Int): String = {

    val matsimOutputDirectory: String = s"$currentDirectory/matsim"
    Files.createDirectories(Paths.get(matsimOutputDirectory))

    val matsimConfig: Config = ConfigUtils.loadConfig(s"$currentDirectory/config.xml")

    matsimConfig.controler().setOutputDirectory(matsimOutputDirectory)
    val scenario: Scenario = ScenarioUtils.loadScenario(matsimConfig)
    val controler: Controler = new Controler(matsimConfig)


    def run(): String = {
      val networkLinks = MATSimNetworkToCollection(s"$currentDirectory/network.xml")
      var currentNetworkState: SnapshotCollector = NewNetworkAnalyticStateCollector(networkLinks, BPRCostFunctionType, timeWindow)
      var currentIteration: Int = 1
      val timeTracker: TimeTracker = TimeTracker(startTime.format(ExperimentFSOps.HHmmssFormat), endTime.format(ExperimentFSOps.HHmmssFormat))

      // add the events handlers
      controler.addOverridingModule(new AbstractModule(){
        @Override def install (): Unit = {
          this.addEventHandlerBinding().toInstance(new SnapshotEventHandler({
            case LinkEventData(e) =>
              val belongsToTimeGroup = timeTracker.belongsToThisTimeGroup(e)
              if (belongsToTimeGroup)
                synchronized {
                  currentNetworkState = NewNetworkAnalyticStateCollector.update(currentNetworkState, e)
                }
            case NewIteration(i) =>
              currentIteration = i
            case o =>
              println("other link data died here")
          }))
        }
      })

      //start the simulation
      controler.run()

      // write snapshot and return filename
      NewNetworkAnalyticStateCollector.toXMLFile(currentDirectory, currentNetworkState) match {
        case Success(file) => file
        case Failure(e) => throw e
      }
    }

    run()
  }

  def MATSimRunUsingGraph(currentDirectory: String, startTime: LocalTime, endTime: LocalTime, timeWindow: Int): List[(Int, Double)] = {

    val matsimOutputDirectory: String = s"$currentDirectory/matsim"
    Files.createDirectories(Paths.get(matsimOutputDirectory))

    val matsimConfig: Config = ConfigUtils.loadConfig(s"$currentDirectory/config.xml")

    matsimConfig.controler().setOutputDirectory(matsimOutputDirectory)
    val scenario: Scenario = ScenarioUtils.loadScenario(matsimConfig)
    val controler: Controler = new Controler(matsimConfig)


    def run(): List[(Int, Double)] = {
      val network = XML.load(ExperimentFSOps.networkFileURI(currentDirectory))
      var graph: LocalGraph = LocalGraphOps.readMATSimXML(EdgesWithDrivers, network)
      var currentIteration: Int = 1
      var timeTracker: TimeTracker = TimeTracker(timeWindow, startTime.format(ExperimentFSOps.HHmmssFormat), endTime.format(ExperimentFSOps.HHmmssFormat))
      val outputList: ListBuffer[(Int,Double)] = ListBuffer()

      // add the events handlers
      controler.addOverridingModule(new AbstractModule(){
        @Override def install (): Unit = {
          this.addEventHandlerBinding().toInstance(new SnapshotEventHandler({
            case LinkEventData(e) =>
              val belongsToTimeGroup = timeTracker.belongsToThisTimeGroup(e)
              if (!belongsToTimeGroup) {
                val currentTotalCongestionCost: Double =
                  if (graph.edges.isEmpty) 0D
                  else graph.edges.flatMap { _._2.attribute.linkCostFlow }.sum
                val entry = (e.time, currentTotalCongestionCost)
                outputList.append(entry)
                timeTracker = timeTracker.advance
              }

              synchronized {
                val edgeId = e.linkID.toString
                graph.edgeById(edgeId) match {
                  case None =>
                  case Some(edge) =>
                    edge.attribute match {
                      case attr: LocalEdgeSimulationAttribute =>
                        val updated = LocalEdgeSimulationAttribute.modifyDrivers(attr, e.vehicleID.toString)
                        graph = graph.updateEdge(edgeId, edge.copy(attribute = updated))
                    }
                }
                //                  currentNetworkState = NewNetworkAnalyticStateCollector.update(currentNetworkState, e)
              }
            case NewIteration(i) =>
              currentIteration = i
            case o =>
              println("other link data died here")
          }))
        }
      })

      //start the simulation
      controler.run()

      outputList.toList
    }

    run()
  }


  /**
    * copies config.xml and network.xml, and updates references in config.xml to this instance directory.
    * this method does not copy population.xml, since different approaches may be taken to furnishing a
    * population.xml for this experiment
    * @param experimentConfigDirectory the source file directory
    * @param experimentInstanceDirectory the directory to (optionally) create and copy these assets into
    */
  def importExperimentConfig(experimentConfigDirectory: String, experimentInstanceDirectory: String): Try[Unit] = {
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
  }



  /**
    * dives into a MATSim config.xml file and alters all values it finds within a <param name="" value=""/> tag (should be one)
    * @param matsimConfig MATSim config.xml file
    * @param moduleName name of a module in the config file
    * @param newFilePath substitute parameter value for the input file of this module
    * @return
    */
  private def modifyModuleValue(matsimConfig: xml.Elem, moduleName: String, newFilePath: String): xml.Elem = {
    val plans = matsimConfig \ "module" filter (_.attribute("name").head.text == moduleName)
    val currentValue = (plans \ "param" \ "@value").text
    if (currentValue.isEmpty) throw new IllegalArgumentException(s"due to the design of Scala's XML library, updates to XML properties is performed by string replacement. The $moduleName value was found to be the empty string, which cannot be used for string replacement.")
    val updated: String = matsimConfig.toString.replace(currentValue, newFilePath)
    Try({XML.loadString(updated)}) match {
      case Success(xml) => xml
      case Failure(e) => throw new IllegalArgumentException(s"XML file deserialization failed when modifying value $currentValue at key $moduleName: ${e.getMessage}")
    }
  }






  /**
    * grabs the MATSim-generated average trip duration value
    * @param instanceDirectory path to the experiment instance directory
    * @return the average trip duration value, or an explanation as to why we couldn't find it
    */
  def getPopulationAvgTravelTime(instanceDirectory: String): String = {
    val tripDurationsRelativePath = "matsim/ITERS/it.0/0.tripdurations.txt"
    val path = s"$instanceDirectory/$tripDurationsRelativePath"
    val regex: Regex = ".*average trip duration: (\\d+.\\d*).*".r

    Try {
      Source.fromFile(path).getLines.mkString
    } match {
      case Success(tripDurationsFile) =>
        tripDurationsFile match {
          case regex(g0) => g0
          case _ => "file found but value not found in file"
        }
      case Failure(e) => s"attempting to load population avg travel time, could not find file $path. ${e.getMessage}"
    }
  }



  /**
    * scrape the network average travel time from the snapshot output file
    * @param instanceDirectory path to the experiment instance directory
    * @return the average network link travel time, or an explanation as to why we couldn't find it
    */
  def getNetworkAvgTravelTime(instanceDirectory: String): String = {
    val path = s"$instanceDirectory/snapshot.xml"

    Try {
      XML.loadFile(path)
    } match {
      case Success(snapshotOutputFile) =>
        Try {
          (snapshotOutputFile \ "global" \ "@avgtraveltime").text
        } match {
          case Success(avgTravelTime) => avgTravelTime
          case Failure(e) => s"found snapshot output file but could not parse root\\global@avgTravelTime. ${e.getMessage}"
        }
      case Failure(e) => s"attempting to load network avg travel time, could not find file $path. ${e.getMessage}"
    }
  }
}
