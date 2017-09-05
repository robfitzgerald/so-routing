package cse.fitzgero.sorouting.app

import cse.fitzgero.sorouting.matsimrunner._

object MATSimSnapshotGenerator extends App {

  // example AppConfig("examples/tutorial/programming/example7-config.xml", "output/example7", "5", "06:00:00", "07:00:00", ArgsNotMissingValues)
  val appConfig: MATSimRunnerConfig = args match {
    case Array(in, out, wD, sT, eT) => MATSimRunnerConfig(in, out, wD.toInt, sT, eT, ArgsNotMissingValues)
    case _ => MATSimRunnerConfig()
  }

  if (appConfig.incomplete) {
    println(s"usage: ")
  } else {
    val outputDirectory: String = MATSimMultipleSnapshotRunnerModule(appConfig).run()
    println(s"resulting snapshot stored in $outputDirectory/snapshot")
  }
}