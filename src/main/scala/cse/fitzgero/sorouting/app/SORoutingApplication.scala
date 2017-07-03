package cse.fitzgero.sorouting.app

import java.time.LocalTime
import java.nio.file.{Files, Paths}

import org.apache.spark.{SparkConf, SparkContext}
import cse.fitzgero.sorouting.algorithm.trafficassignment.TrafficAssignment
import cse.fitzgero.sorouting.matsimrunner._
import cse.fitzgero.sorouting.util.{SORoutingApplicationConfig, SORoutingParseArgs}

/**
  * Base level application for running SORouting
  */
object SORoutingApplication extends App {

//  val conf: SORoutingApplicationConfig = SORoutingParseArgs(args)

  val conf = SORoutingApplicationConfig("config.xml", "result", "*", 5, 100, LocalTime.parse("06:00"), LocalTime.parse("19:00"))

  val sparkConf = new SparkConf()
    .setAppName("cse.fitzgero.app.SORoutingApplication")
    .setMaster(s"local[${conf.sparkProcesses}]")
    .set("spark.executor.memory","1g")
  val sc = new SparkContext(sparkConf)

  //
  // $WORKING_DIR
  //   config.xml
  //   config-full-ue.xml
  //   config-partial-ue.xml
  //   config-combined-ue-so.xml
  //   population-generated.xml
  //   population-routed.xml
  //   snapshot/
  //   full-ue-results/
  //   partial-ue-results/
  //   combined-ue-so-results/
  //   results/
  //

  val runSnapshotGenerator: Boolean = Files.exists(Paths.get(conf.workingDirectory + "/snapshot"))

  //----------------------------------------------------------------------------------------------
  //  1. Run 100% UE Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  // TODO: determine how to measure congestion
  MATSimRunnerModule(MATSimRunnerConfig(
    conf.workingDirectory + "/config-full.ue.xml",
    conf.workingDirectory + "/full-ue-results"
  ))

  //----------------------------------------------------------------------------------------------
  //  2. Run 1-p% UE Simulation, get snapshots
  //----------------------------------------------------------------------------------------------
  // TODO: parameterize p
  // TODO: sampling method by percentage by positional features (startx, starty, endx, endy from pop. file)



  //----------------------------------------------------------------------------------------------
  //  3. For each snapshot, load as graphx and run our algorithm
  //----------------------------------------------------------------------------------------------
  // TODO: get list of snapshot files in snapshot directory
  // TODO: implement shortest path
  // TODO: implement gradient descent
  // TODO: update the entry of a person in the population file to have new path from path search

  //----------------------------------------------------------------------------------------------
  //  4. Run 1-p% UE UNION p% SO Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  // TODO: determine how to measure congestion

  //----------------------------------------------------------------------------------------------
  //  5. Analyze Results (what kinds of analysis?)
  //----------------------------------------------------------------------------------------------
  // TODO: determine how to measure results


  val appConfig: MATSimRunnerConfig = args match {
    case Array(in, out, wD, sT, eT) => MATSimRunnerConfig(in, out, wD, sT, eT, ArgsNotMissingValues)
    case _ => MATSimRunnerConfig()
  }
  if (appConfig.incomplete) {
    println(s"usage: ")
  } else {
    val outputDirectory: String = MATSimSnapshotRunnerModule(appConfig).run()
    println(s"resulting snapshot stored in $outputDirectory/snapshot")
  }

  println(s"~~~ SO Routing Algorithm ~~~")

  sc.stop()
}
