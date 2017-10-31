package cse.fitzgero.sorouting.experiments.ops

import java.nio.file.{Files, Paths}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}
import scala.xml.dtd.{DocType, SystemID}

object ExperimentFSOps {
  // MATSim XML DocTypes for writing new files
  val WriteXmlDeclaration: Boolean = true
  val UTF8: String = "UTF-8"
  val ConfigDocType: DocType = DocType("config", SystemID("http://www.matsim.org/files/dtd/config_v1.dtd"), Nil)
  val NetworkDocType: DocType = DocType("network", SystemID("http://www.matsim.org/files/dtd/network_v1.dtd"), Nil)
  val PopulationDocType: DocType = DocType("population", SystemID("http://www.matsim.org/files/dtd/population_v6.dtd"), Nil)

  def populationFileURI(path: String): String = s"$path/population.xml"
  def networkFileURI(path: String): String = s"$path/network.xml"
  def configFileURI(path: String): String = s"$path/config.xml"
  def snapshotFileURI(path: String): String = s"$path/snapshot.xml"
  def selfishDirectory(path: String): String = s"$path/selfish"
  def optimalDirectory(path: String): String = s"$path/optimal"

  val HHmmssFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

  /**
    * given the instance path of an experiment, test to find if a population.xml file exists in the most recent
    * previous instance
    * @param path
    * @return
    */
  def findPreviousPopulation(path: String): Option[String] = {
    val found = Files
      .list(Paths.get(path))
      .iterator.asScala.toSeq
      .filter(pathFound =>
        Try{
          LocalDateTime.parse(pathFound.getFileName.toString) // blows up -> false
          val populationFileFound = Files.exists(Paths.get(s"$pathFound/population.xml"))
          populationFileFound
        } match {
          case Success(popFound) => popFound
          case Failure(_) => false
        }
      )
    found
      .map(_.toString)
      .sorted
      .lastOption match {
      case None => None
      case Some(dir) =>
        Some(s"$dir/population.xml")
    }
  }

}
