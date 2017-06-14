package cse.fitzgero.sorouting.app

import org.apache.spark.{SparkConf, SparkContext}
import cse.fitzgero.sorouting.algorithm.trafficassignment.SGDSolver
import cse.fitzgero.sorouting.matsimrunner._

/**
  * Base level application for running SORouting
  */
object SORoutingApplication extends App {
  val conf = new SparkConf()
    .setAppName("cse.fitzgero.app.SORoutingApplication")
    .setMaster("local[*]")
    .set("spark.executor.memory","1g")
  val sc = new SparkContext(conf)
  // example AppConfig("examples/tutorial/programming/example7-config.xml", "output/example7", "5", "06:00:00", "07:00:00", ArgsNotMissingValues)
  // AppConfig("examples/equil/config.xml", "result", 5, 05:00:00, 07:00:00, ArgsNotMissingValues)

  //----------------------------------------------------------------------------------------------
  //  1. Run 100% UE Simulation, get overall congestion (measure?)
  //----------------------------------------------------------------------------------------------
  // TODO: determine how to measure congestion

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


  val appConfig: AppConfig = args match {
    case Array(in, out, wD, sT, eT) => AppConfig(in, out, wD, sT, eT, ArgsNotMissingValues)
    case _ => AppConfig()
  }
  if (appConfig.incomplete) {
    println(s"usage: ")
  } else {
    val outputDirectory: String = MATSimRunnerModule(appConfig).run()
    println(s"resulting snapshot stored in $outputDirectory/snapshot")
  }

  println(s"~~~ SO Routing Algorithm ~~~")
  println(s"~~~~~ ${SGDSolver.foo()} ~~~~~")

  sc.stop()
}
