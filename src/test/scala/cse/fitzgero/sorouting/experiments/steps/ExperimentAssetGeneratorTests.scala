package cse.fitzgero.sorouting.experiments.steps

import java.nio.file.{Files, Paths}
import java.time.{LocalDateTime, LocalTime}

import cse.fitzgero.sorouting.FileWriteSideEffectTestTemplate

import scala.xml.XML

class ExperimentAssetGeneratorTests extends FileWriteSideEffectTestTemplate("ExperimentAssetGenerator") {
  "ExperimentAssetGenerator" when {
    "ImportExperiment" when {
      "called with a valid source and destination" should {
        "copy the network and config files into the destination and update the config's URIs" in {
          val sourceDir = "data/5x5"
          val destDir = s"$testRootPath/testInstance"
          case class Config(experimentConfigDirectory: String, experimentInstanceDirectory: String)
          val config = Config(sourceDir, destDir)

          ExperimentAssetGenerator.SetupInstanceDirectory(config) match {
            case None => fail()
            case Some(result) =>
              Files.exists(Paths.get(s"$destDir/network.xml")) should be (true)
              Files.exists(Paths.get(s"$destDir/config.xml")) should be (true)

              // add a test that inspects and confirms the config values for modules "network" and "plans"
          }
        }
      }
    }
    "LoadStoredPopulation" when {
      "called with a valid population file path and config" should {
        "copy it into a test destination" in {
          val populationFilePath: String = "src/test/resources/PopulationGeneratorTests/plans100.xml"
          case class Config(loadStoredPopulationPath: String, experimentInstanceDirectory: String)
          val config = Config(populationFilePath, testRootPath)

          ExperimentAssetGenerator.LoadStoredPopulation(config, Map.empty[String, Map[String, String]]) match {
            case Some(result) =>
              val xml = XML.loadFile(s"$testRootPath/population.xml")
              (xml \ "person").size should equal (100)
            case None =>
              fail()
          }
        }
      }
    }
    "Repeated" when {
      case class Config(populationSize: Int, departTime: LocalTime, endTime: Option[LocalTime], timeDeviation: Option[LocalTime], sourceAssetsDirectory: String, experimentConfigDirectory: String, experimentInstanceDirectory: String)
      "called once" should {
        "generate a population file" in {
          val srcDirectory: String = "src/test/resources/PopulationGeneratorTests"
          val populationSize: Int = 50
          val config = Config(populationSize, LocalTime.parse("08:00:00"), Some(LocalTime.parse("10:00:00")), None, srcDirectory, testRootPath, testRootPath)

          ExperimentAssetGenerator.RepeatedPopulation(config, Map.empty[String, Map[String, String]]) match {
            case None => fail()
            case Some(result) =>
              val xml = XML.loadFile(s"$testRootPath/population.xml")

              // the population should be generated with the correct size
              (xml \ "person").size should equal (populationSize)
          }
        }
      }
      "called twice" should {
        "generate a population file, then copy it on the next instance" in {
          val srcDirectory: String = "src/test/resources/PopulationGeneratorTests"
          val populationSize: Int = 50
          val configDirectory: String = testRootPath
          val firstInstanceDirectory: String = s"$testRootPath/${LocalDateTime.now.toString}"
          val secondInstanceDirectory: String = s"$testRootPath/${LocalDateTime.now.toString}"
          require(firstInstanceDirectory != secondInstanceDirectory, "test should generate two different instance directories, but it's theoretically possible it runs too fast and generates a duplicate")
          val firstConfig = Config(populationSize, LocalTime.parse("08:00:00"), Some(LocalTime.parse("10:00:00")), None, srcDirectory, configDirectory, firstInstanceDirectory)
          val secondConfig = Config(populationSize, LocalTime.parse("08:00:00"), Some(LocalTime.parse("10:00:00")), None, srcDirectory, configDirectory, secondInstanceDirectory)

          ExperimentAssetGenerator.RepeatedPopulation(firstConfig, Map.empty[String, Map[String, String]]) match {
            case None => fail()
            case Some(result) =>
              ExperimentAssetGenerator.RepeatedPopulation(secondConfig, Map.empty[String, Map[String, String]]) match {
                case None => fail()
                case Some(secondResult) =>
                  val firstXml = XML.loadFile(s"$firstInstanceDirectory/population.xml")
                  val secondXml = XML.loadFile(s"$secondInstanceDirectory/population.xml")
                  val firstPersonIds = (firstXml \ "person").map(_.attributes.asAttrMap("id")).toSet
                  val secondPersonIds = (secondXml \ "person").map(_.attributes.asAttrMap("id")).toSet

                  // both populations should be of the correct size
                  firstPersonIds.size should equal (populationSize)
                  secondPersonIds.size should equal (populationSize)

                  // the ids of the persons should be the same
                  firstPersonIds should equal (secondPersonIds)
              }
          }
        }
      }
    }
    "Unique" when {
      case class Config(populationSize: Int, departTime: LocalTime, endTime: Option[LocalTime], timeDeviation: Option[LocalTime], sourceAssetsDirectory: String, experimentConfigDirectory: String, experimentInstanceDirectory: String)
      "called multiple times" should {
        "result in populations that are unique each time" in {
          val srcDirectory: String = "src/test/resources/PopulationGeneratorTests"
          val populationSize: Int = 50
          val configDirectory: String = testRootPath
          val firstInstanceDirectory: String = s"$testRootPath/${LocalDateTime.now.toString}"
          val secondInstanceDirectory: String = s"$testRootPath/${LocalDateTime.now.toString}"
          require(firstInstanceDirectory != secondInstanceDirectory, "test should generate two different instance directories, but it's theoretically possible it runs too fast and generates a duplicate")
          val firstConfig = Config(populationSize, LocalTime.parse("08:00:00"), Some(LocalTime.parse("10:00:00")), None, srcDirectory, configDirectory, firstInstanceDirectory)
          val secondConfig = Config(populationSize, LocalTime.parse("08:00:00"), Some(LocalTime.parse("10:00:00")), None, srcDirectory, configDirectory, secondInstanceDirectory)

          ExperimentAssetGenerator.UniquePopulation(firstConfig, Map.empty[String, Map[String, String]]) match {
            case None => fail()
            case Some(firstResult) =>
              ExperimentAssetGenerator.UniquePopulation(secondConfig, Map.empty[String, Map[String, String]]) match {
                case None => fail()
                case Some(secondResult) =>
                  val firstXml = XML.loadFile(s"$firstInstanceDirectory/population.xml")
                  val secondXml = XML.loadFile(s"$secondInstanceDirectory/population.xml")
                  val firstPersonIds = (firstXml \ "person").map(_.attributes.asAttrMap("id")).toSet
                  val secondPersonIds = (secondXml \ "person").map(_.attributes.asAttrMap("id")).toSet
                  firstPersonIds should not equal secondPersonIds
              }
          }
        }
      }
    }
  }
}
