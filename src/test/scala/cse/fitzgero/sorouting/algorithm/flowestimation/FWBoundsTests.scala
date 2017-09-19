package cse.fitzgero.sorouting.algorithm.flowestimation

import java.time.Instant

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class FWBoundsTests extends SORoutingUnitTestTemplate {
  "TerminationCriteria" when {
    val startTime: Long = Instant.now().toEpochMilli
    val tenIterations: Int = 10
    val fivePercentRelGap: Double = 0.05D
    val testData = TerminationData(startTime, tenIterations, fivePercentRelGap)
    "RelativeGapTerminationCriteria" when {
      "called when within a relative gap threshold" should {
        "evaluate true" in {
          RelativeGapFWBounds(0.06).eval(testData) should equal (true)
        }
      }
      "called when not within a relative gap threshold" should {
        "evaluate false" in {
          RelativeGapFWBounds(0.05).eval(testData) should equal (false)
        }
      }
    }
    "IterationTerminationCriteria" when {
      "called when we have performed enough iterations to stop" should {
        "evaluate true" in {
          IterationFWBounds(10).eval(testData) should equal (true)
        }
      }
      "called when we have not performed enough iterations to stop" should {
        "evaluate false" in {
          IterationFWBounds(11).eval(testData) should equal (false)
        }
      }
    }
    "RunningTimeTerminationCriteria" when {
      "called when enough time has passed to stop" should {
        "evaluate true" in {
          val startedInPast = startTime - 60000L
          val testData = TerminationData(startedInPast, tenIterations, fivePercentRelGap)
          RunningTimeFWBounds(1000L).eval(testData) should equal (true)
        }
      }
      "called when not enough time has passed to stop" should {
        "evaluate false" in {
          RunningTimeFWBounds(8640000000L).eval(testData) should equal (false)
        }
      }
    }
    "CombinedTerminationCriteria" when {
      "called when both thresholds have been passed and aggregated via the provided operation" should {
        "evaluate true" in {
          CombinedFWBounds(
            RelativeGapFWBounds(0.06),  // passes
            And,
            IterationFWBounds(10)
          ).eval(testData) should equal (true)
          CombinedFWBounds(
            RelativeGapFWBounds(0.05),
            Or,
            IterationFWBounds(10)      // passes
          ).eval(testData) should equal (true)
          CombinedFWBounds(
            RelativeGapFWBounds(0.06),  // passes
            Or,
            IterationFWBounds(11)
          ).eval(testData) should equal (true)
        }
      }
      "called when no threshold has been passed" should {
        "evaluate false" in {
          CombinedFWBounds(
            RelativeGapFWBounds(0.04),
            And,
            IterationFWBounds(11)
          ).eval(testData) should equal (false)
        }
      }
    }
  }
}
