package cse.fitzgero.sorouting.experiments.steps

import java.nio.file.{Files, Paths}
import java.time.{LocalDateTime, LocalTime}

import cse.fitzgero.sorouting.FileWriteSideEffectTestTemplate
import edu.ucdenver.fitzgero.lib.experiment.StepFailure

class MATSimRunnerTests extends FileWriteSideEffectTestTemplate("MATSimRunner"){
  "MATSimRunner" when {
    "MATSimSnapshotRunner" when {
      "called with valid config file and config values" should {
        "run a matsim simulation and halt, writing analytics to a snapshot file" in {
          val configDirectory: String = s"data/5x5"
          val networkURI: String = s"$configDirectory/network.xml"
          val instanceDirectory: String = s"$testRootPath/${LocalDateTime.now.toString}"
          Files.createDirectories(Paths.get(instanceDirectory))
          val startTime = LocalTime.parse("08:00:00")
          val endTime = LocalTime.parse("08:10:00")
          case class PopulationConfig (networkURI: String, experimentInstanceDirectory: String, populationSize: Int, startTime: LocalTime, endTime: Option[LocalTime])
          case class ImportConfig (experimentConfigDirectory: String, experimentInstanceDirectory: String)
          case class MATSimConfig(experimentInstanceDirectory: String, timeWindow: Int, startTime: LocalTime, endTime: Option[LocalTime])
          val populationConfig = PopulationConfig(networkURI, instanceDirectory, 100, startTime, Some(endTime))
          val importConfig = ImportConfig(configDirectory, instanceDirectory)
          val matsimConfig = MATSimConfig(instanceDirectory, 60, startTime, Some(endTime))

          ExperimentAssetGenerator.Unique(populationConfig) match {
            case None => fail("was unable to generate a population for this test")
            case Some(populationResult) =>
              populationResult._1 should not be a [StepFailure]
            ExperimentAssetGenerator.SetupInstanceDirectory(importConfig) match {
              case None => fail("was unable to copy in config assets")
              case Some(importResult) =>
                importResult._1 should not be a [StepFailure]
                MATSimRunner.AnalyticSnapshot(matsimConfig) match {
                  case None => fail()
                  case Some(matsimResult) =>
                    Files.exists(Paths.get(matsimResult._2("fs.xml.snapshot"))) should equal (true)
                    println(matsimResult)
                }

            }
          }

        }
      }
    }
  }
}
