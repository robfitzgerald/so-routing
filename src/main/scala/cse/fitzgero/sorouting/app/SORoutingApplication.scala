package cse.fitzgero.sorouting.app

import org.apache.spark.{SparkConf, SparkContext}

import cse.fitzgero.sorouting.algorithm.trafficassignment.SGDSolver
import cse.fitzgero.sorouting.roadnetwork.graph.GraphXRoadNetwork

/**
  * Base level application for running SORouting
  */
object SORoutingApplication extends App {
  val conf = new SparkConf()
    .setAppName("cse.fitzgero.app.SORoutingApplication")
    .setMaster("local[*]")
    .set("spark.executor.memory","1g")
  val sc = new SparkContext(conf)

  // work here
  println(s"~~~ SO Routing Algorithm ~~~")
  println(s"~~~~~ ${SGDSolver.foo()} ~~~~~")

  sc.stop()
}
