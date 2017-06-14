package cse.fitzgero.sorouting.app

import cse.fitzgero.sorouting.matsimrunner._

object MATSimSnapshotGenerator extends App {

  // example AppConfig("examples/tutorial/programming/example7-config.xml", "output/example7", "5", "06:00:00", "07:00:00", ArgsNotMissingValues)
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
}