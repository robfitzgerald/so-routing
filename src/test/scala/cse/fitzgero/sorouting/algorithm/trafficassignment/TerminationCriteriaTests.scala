package cse.fitzgero.sorouting.algorithm.trafficassignment

import java.time.Instant

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class TerminationCriteriaTests extends SORoutingUnitTestTemplate {
  "TerminationCriteria" when {
    val startTime: Long = Instant.now().toEpochMilli
    val tenIterations: Int = 10
    val fiftyPercentRelGap: Double = 0.5D
    val testData = TerminationData(startTime, tenIterations, fiftyPercentRelGap)
    "RelativeGapTerminationCriteria" when {
      "called when within a relative gap threshold" should {
        "evaluate true" in {
          RelativeGapTerminationCriteria(0.6).eval(testData) should equal (true)
        }
      }
      "called when not within a relative gap threshold" should {
        "evaluate false" in {
          RelativeGapTerminationCriteria(0.5).eval(testData) should equal (false)
        }
      }
    }
    "IterationTerminationCriteria" when {
      "called when we have performed enough iterations to stop" should {
        "evaluate true" in {
          IterationTerminationCriteria(10).eval(testData) should equal (true)
        }
      }
      "called when we have not performed enough iterations to stop" should {
        "evaluate false" in {
          IterationTerminationCriteria(11).eval(testData) should equal (false)
        }
      }
    }
    "RunningTimeTerminationCriteria" when {
      "called when enough time has passed to stop" should {
        "evaluate true" in {
          val startedInPast = startTime - 60000L
          val testData = TerminationData(startedInPast, tenIterations, fiftyPercentRelGap)
          RunningTimeTerminationCriteria(1000L).eval(testData) should equal (true)
        }
      }
      "called when not enough time has passed to stop" should {
        "evaluate false" in {
          RunningTimeTerminationCriteria(8640000000L).eval(testData) should equal (false)
        }
      }
    }
    "CombinedTerminationCriteria" when {
      "called when both thresholds have been passed and aggregated via the provided operation" should {
        "evaluate true" in {
          CombinedTerminationCriteria(
            RelativeGapTerminationCriteria(0.6),  // passes
            And,
            IterationTerminationCriteria(10)
          ).eval(testData) should equal (true)
          CombinedTerminationCriteria(
            RelativeGapTerminationCriteria(0.5),
            Or,
            IterationTerminationCriteria(10)      // passes
          ).eval(testData) should equal (true)
          CombinedTerminationCriteria(
            RelativeGapTerminationCriteria(0.6),  // passes
            Or,
            IterationTerminationCriteria(11)
          ).eval(testData) should equal (true)
        }
      }
      "called when no threshold has been passed" should {
        "evaluate false" in {
          CombinedTerminationCriteria(
            RelativeGapTerminationCriteria(0.4),
            And,
            IterationTerminationCriteria(11)
          ).eval(testData) should equal (false)
        }
      }
    }
  }
}
