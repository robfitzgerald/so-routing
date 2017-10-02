package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class PopulationTimeGeneratorTests extends SORoutingUnitTestTemplate {
  "PopulationRandomTimeGenerator" when {
    "created with time data, and called" should {
      "produce a tuple of time data" in {
        val populationRandomTimeGenerator = PopulationRandomTimeGenerator(
          Seq(
            ("home", BidirectionalDeviation(LocalTime.parse("09:00:00"), 10L, 1L)),
            ("work", RangeDeviation(LocalTime.parse("08:00:00"), -20L, 5L, 1L))
          )
        )
        val result: GeneratedTimeValues = populationRandomTimeGenerator.next()
        result.foreach(a => println(a.toString))
        result("home").getHour should equal (9)
        result("home").getMinute should equal (0)
        result("home").getSecond should equal (15)
        result("work").getHour should equal (7)
        result("work").getMinute should equal (59)
        result("work").getSecond should equal (50)
      }
    }
  }
}
