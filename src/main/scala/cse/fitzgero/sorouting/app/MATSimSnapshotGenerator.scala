package cse.fitzgero.sorouting.app

import cse.fitzgero.sorouting.matsimrunner._
import cse.fitzgero.sorouting.util.ClassLogging

object MATSimSnapshotGenerator extends App with ClassLogging {

  val appConfig: MATSimRunnerConfig = args match {
    case Array(in, out, wD, sT, eT) => MATSimRunnerConfig(in, out, wD.toInt, sT, eT, ArgsNotMissingValues)
    case _ => MATSimRunnerConfig()
  }

  if (appConfig.incomplete) {
    println(s"usage: ")
  } else {
    val outputDirectory: String = MATSimMultipleSnapshotRunnerModule(appConfig).run()
    logger.info(s"resulting snapshot stored in $outputDirectory/snapshot")
  }
}