package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.util.convenience._

class PopulationRandomTimeGeneratorTests extends SORoutingUnitTestTemplate {
  "PopulationRandomTimeGenerator" when {
    "instantiated with a ActivityTime object" should {
      "produce predictable results" in {
        val activities =
          Seq(
            ActivityConfig2(
              "home",
              LocalTime.parse("09:00:00") endTime,
              30 minutesDeviation),
            ActivityConfig2(
              "work",
              LocalTime.parse("17:00:00") endTime,
              30 minutesDeviation),
            ActivityConfig2(
              "home",
              LocalTime.MIDNIGHT endTime,
              30 minutesDeviation)
          )

        val generator = PopulationRandomTimeGenerator2(activities)

        println(generator.next())
        println(generator.next())
        println(generator.next())
      }
    }
  }
}
