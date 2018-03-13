package cse.fitzgero.sorouting.experiments.ops

import java.time.LocalTime

import cse.fitzgero.sorouting.{FileWriteSideEffectTestTemplate, SORoutingUnitTestTemplate}

class MATSimOpsTests extends FileWriteSideEffectTestTemplate("MATSimRunGenerateSensorSimulationData") {
  "MATSimRunGenerateSensorSimulationData" when {
    "Generate Data for Students" should {
      "produce a bunch of data" in {
        val file = "result/20180306/rye-6000-15-0.2-1.1/2018-03-06T11:45:17.776"
        val result = MATSimOps.MATSimRunGenerateSensorSimulationData(file, LocalTime.parse("08:00:00"), LocalTime.parse("11:00:00"))
        println(result.mkString("\n"))
      }
    }
  }
}
