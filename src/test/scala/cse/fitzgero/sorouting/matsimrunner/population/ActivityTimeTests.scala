package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class ActivityTimeTests extends SORoutingUnitTestTemplate {
  "ActivityTimeAndDeviation" when {
    "BidirectionalDeviation" when {
      "created and nextTime() is called" should {
        "produce a time from a gaussian distribution" in {
          val result = BidirectionalDeviation(LocalTime.parse("09:00:00"), 10L, 1L)
          // from seed value, all results should be predictable - test first result
          result.nextTime().format(DateTimeFormatter.ofPattern("HH:mm")) should equal ("09:15")
          // property test - 100 values within bounds of 3 standard deviations
          (0 to 100).foreach(_ => {
            val next = result.nextTime()
            next.getHour should (be (8) or be (9))
            if (next.getHour == 8)
              next.getMinute should be >= 30
            else
              next.getMinute should be <= 30
          })
        }
      }
    }
    "RangeDeviation" when {
      "created and nextTime() is called" should {
        "produce a time from a gaussian distribution with the provided 1st std dev range" in {
          val result = RangeDeviation(LocalTime.parse("09:00:00"), -5L, 20L, 1L)
          // from seed value, all results should be predictable - test first result
          result.nextTime().format(DateTimeFormatter.ofPattern("HH:mm")) should equal ("09:10")
        }
      }
    }
    "BidirectionalBoundedDeviation" when {
      "created and nextTime() is called" should {
        "produce a time from a bounded gaussian distribution" in {
          val result = BidirectionalBoundedDeviation(LocalTime.parse("09:00:00"), 10L, 1L)
          // from seed value, all results should be predictable - test first result
          result.nextTime().format(DateTimeFormatter.ofPattern("HH:mm")) should equal ("09:07")
          // property test - 100 values within bounds parameter
          (0 to 100).foreach(_ => {
            val next = result.nextTime()
            next.getHour should (be (8) or be (9))
            if (next.getHour == 8)
              next.getMinute should be >= 50
            else
              next.getMinute should be <= 10
          })
        }
      }
    }
  }
}
