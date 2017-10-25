package cse.fitzgero.sorouting.experiments

import java.nio.file.{Files, Paths}
import java.time.LocalDateTime

import scala.util.{Failure, Success, Try}
import scala.xml.dtd.{DocType, SystemID}
import scala.collection.JavaConverters._

object ExperimentFSOps {
  // MATSim XML DocTypes for writing new files
  val WriteXmlDeclaration = true
  val ConfigDocType = DocType("config", SystemID("http://www.matsim.org/files/dtd/config_v1.dtd"), Nil)
  val NetworkDocType = DocType("network", SystemID("http://www.matsim.org/files/dtd/network_v1.dtd"), Nil)
  val PopulationDocType = DocType("population", SystemID("http://www.matsim.org/files/dtd/population_v6.dtd"), Nil)

  def populationFileURI(path: String): String = s"$path/population.xml"
  def networkFileURI(path: String): String = s"$path/network.xml"
  def configFileURI(path: String): String = s"$path/config.xml"

  def findDateTimeStrings(path: String): Seq[String] =
    Files
      .list(Paths.get(""))
      .iterator.asScala.toSeq
      .map(_.toString)
      .filter(path =>
        Try(
          LocalDateTime.parse(path)
        ) match {
          case Success(path) => true
          case Failure(e) => false
        }
      )
}
