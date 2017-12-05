package cse.fitzgero.sorouting.experiments.ops

import java.io.File
import java.nio.file.{Files, Paths}
import java.time.{Instant, LocalDateTime}
import java.time.format.DateTimeFormatter

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}
import scala.xml.{Elem, XML}
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

  def saveXmlDocType(uri: String, xmlElement: Elem, docType: DocType): String = {
    XML.save(uri, xmlElement, ExperimentFSOps.UTF8, ExperimentFSOps.WriteXmlDeclaration, docType)
    // TODO: a spin wait that tests that the file exists. uncertain if XML.save is an async utility. do we want this?
    //      val endLoop: Long = Instant.now.toEpochMilli + 20000L
    //      while (Instant.now.toEpochMilli < endLoop && !Files.exists(Paths.get(uri))) { Thread.sleep(100) }
    //      // (wrap in Try)
    uri
  }

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



  /**
    * recursive directory tree delete
    * @param path the root of the tree to delete
    * @return
    */
  def recursiveDelete(path: String): String= {
    def delete(file: File): Array[(String, Boolean)] = {
      Option(file.listFiles).map(_.flatMap(f => delete(f))).getOrElse(Array()) :+ (file.getPath -> file.delete)
    }

    Try({
      //      Files.deleteIfExists(Paths.get(testRootPath))
      val file: File = new File(path)
      delete(file).filter(_._2).map(_._1).mkString("\n")
    })
  } match {
    case Success(filesDeleted) => filesDeleted
    case Failure(e) =>
      println(s"recursive delete exiting with an error. $e")
      e.getMessage
  }



  /**
    * helper that creates a tmp directory and copies basic assets inside this instance directory
    * @param experimentInstanceDirectory instance directory path
    * @return temp directory path
    */
  def importAssetsToTempDirectory(experimentInstanceDirectory: String): String = {
    MATSimOps.importExperimentConfig(experimentInstanceDirectory, s"$experimentInstanceDirectory/tmp") match {
      case Success(_) => s"$experimentInstanceDirectory/tmp"
      case Failure(e) => throw e
    }
  }
}
