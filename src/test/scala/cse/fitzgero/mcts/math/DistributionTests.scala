package cse.fitzgero.mcts.math

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class DistributionTests extends SORoutingUnitTestTemplate {
  "Distribution" when {
    "constructed without an argument" should {
      "correctly represent an empty distribution" in {
        val test = Distribution()
        test.isEmpty should equal (true)
        test.min.isEmpty should be (true)
        test.max.isEmpty should be (true)
        test.mean.isEmpty should be (true)
        test.sampleVariance.isEmpty should be (true)
        test.standardDeviation.isEmpty should be (true)
        test.count should equal (0)
      }
    }
    "constructed with a value" should {
      "correctly represent a singleton Distribution" in {
        val testValue = 4D
        val test = Distribution(testValue)
        test.min.get should equal (testValue)
        test.max.get should equal (testValue)
        test.mean.get should equal (testValue)
        test.sampleVariance.get should equal (0D)
        test.count should equal (1)
        test.standardDeviation.get should equal (0D)
      }
    }
    "+" when {
      "given a distribution and a new sample" should {
        "correctly update the Distribution" in {
          val firstValue = 4D
          val secondValue = 8D
          val test = Distribution(firstValue) + secondValue
          test.min.get should equal (firstValue)
          test.max.get should equal (secondValue)
          test.mean.get should equal ((firstValue + secondValue) / 2)
          test.count should equal (2)
          test.sampleVariance.get should equal (8D)
          test.standardDeviation.get should equal (math.sqrt(8D))
        }
      }
      "given a distribution and many new samples" should {
        "website numbers to compare with" in {
          val test = ((((Distribution(3) + 21) + 98) + 203 )+ 17) + 9
          test.min.get should equal(3.0D)
          test.max.get should equal(203.0D)
          math.floor(test.mean.get) should equal(58.0D)
          test.count should equal(6)
          test.sampleVariance.get should equal (6219.9)
          math.round(test.standardDeviation.get) should equal (79)
        }
      }
    }
    "++" when {
      "given two small Distributions" ignore {
        "properly combine them" in {
          // ++ not yet implemented
          val left = Distribution(2) + 4
          val right = Distribution(6) + 8
//          val result = left ++ right
//          println(result)

        }
      }
    }
  }
}
