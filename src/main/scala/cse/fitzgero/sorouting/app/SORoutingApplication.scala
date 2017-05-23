package cse.fitzgero.sorouting.app

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Base level application for running SORouting
  */
object SORoutingApplication extends App {
  val conf = new SparkConf().setAppName("cse.fitzgero.app.SORoutingApplication")
  val sc = new SparkContext(conf)

  // work here

  sc.stop()
}
