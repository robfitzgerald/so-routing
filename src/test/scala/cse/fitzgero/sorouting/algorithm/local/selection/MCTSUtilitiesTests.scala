package cse.fitzgero.sorouting.algorithm.local.selection

import scala.collection.GenMap

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.local.selection.SelectionLocalMCTSAlgorithm.{MCTSAltPath, MCTSTreeNode, PersonID, Tag}
import org.scalatest.prop.PropertyChecks
//import org.scalacheck.Prop.forAllNoShrink
import org.scalacheck.Gen
import org.scalacheck.Arbitrary.arbitrary

class MCTSUtilitiesTests extends SORoutingUnitTestTemplate with PropertyChecks {
  "MCTSPseudorandomUtilities" when {
    val congestionThreshold: Double = 1.1D
    val justBelowThreshold: Double = 1.09D
    val justAboveThreshold: Double = 1.2D
    val genListThatShouldProduceAReward = Gen.containerOf[List, (String, Double, Double)] {
      for {
        name <- arbitrary[String]
        original <- Gen.oneOf(Gen.choose(10D, 1000.0D), Gen.const(0.0D))
        added <- Gen.choose(original, justBelowThreshold * original)
      } yield (name, original, added)
    }
    val genListMeanAboveThreshold = Gen.containerOf[List, (String, Double, Double)] {
      Gen.frequency(
        (8, for {
            name <- arbitrary[String]
            original <- Gen.choose(10D, 1000.0D)
            added <- Gen.choose(original, justAboveThreshold * original)
          } yield (name, original, added)
        ),
        (2, for {
            name <- arbitrary[String]
            original <- Gen.choose(10D, 1000.0D)
            added <- Gen.choose(original, justBelowThreshold * original)
          } yield (name, original, added)
        )
      )
    }

    "random" when {
      "is called with a seed value" should {
        "give a predictable pseudorandom result" in {
          val test = MCTSUtilities(Some(1), 0.0D, Map())
          test.random.nextInt should equal(-1155869325)
        }
      }
    }
    "forAllCostDiff" when {
      "is called with a set of values where one increases faster than CongestionRatioThreshold" should {
        "return a score of 0" in {
          val forAllCostDiff: (List[(String, Double, Double)]) => Int =
            MCTSUtilities(Some(1), congestionThreshold, Map()).forAllCostDiff

          forAll (genListThatShouldProduceAReward) { (n: List[(String, Double, Double)]) =>
            whenever (
              n.nonEmpty &&
              n.exists {
                tup => // this is to prevent incorrect values due to ScalaCheck Shrinkage
                  tup._2 > 0 && tup._3 > 0 && tup._2 <= tup._3
              }
            ) {
              val modifiedList = n
                .zipWithIndex
                .find {
                  tup => tup._1._2 > 0
                } match {
                  case None => fail
                  case Some(t) =>
                    val index = t._2
                    n.updated(index, ("breaks it", n(index)._2, n(index)._2 * justAboveThreshold))
                }

              forAllCostDiff(modifiedList) should equal (0)
            }
          }
        }
      }
      "is called with a set of values where nothing increases faster than CongestionRatioThreshold" should {
        "return a score of 1" in {
          val forAllCostDiff: (List[(String, Double, Double)]) => Int =
            MCTSUtilities(Some(1), congestionThreshold, Map()).forAllCostDiff

          forAll (genListThatShouldProduceAReward) { (n: List[(String, Double, Double)]) =>
            forAllCostDiff(n) should equal (1)
          }
        }
      }
    }
    "meanCostDiff" when {
      "is called with a set of at least two values, where one increases faster than CongestionRatioThreshold, but the rest are just below the threshold" should {
        "return a score of 1" in {
          val meanCostDiff: (List[(String, Double, Double)]) => Int =
            MCTSUtilities(Some(1), congestionThreshold, Map()).meanCostDiff

          forAll (genListThatShouldProduceAReward) { (n: List[(String, Double, Double)]) =>
            whenever (
              n.size > 1 &&
              n.forall {
              tup => // this is to prevent incorrect values due to ScalaCheck Shrinkage
                tup._2 >= 0 && tup._3 >= 0 && tup._2 <= tup._3
              }
            ) {
              val index = scala.util.Random.nextInt(n.size)
              val modifiedList = n.updated(index, ("shouldn't break it", n.head._2, n.head._2 * justAboveThreshold))
              meanCostDiff(modifiedList) should equal (1)
            }
          }
        }
      }
      "is called with a set of at least two values, where enough increase faster than CongestionRatioThreshold to result in a mean increase greater than the congestion ratio" ignore {
        "return a score of 0" in {
          val meanCostDiff: (List[(String, Double, Double)]) => Int =
            MCTSUtilities(Some(1), congestionThreshold, Map()).meanCostDiff

          // TODO: issues with ScalaCheck Shrinkage pushing numbers down to 0.0. How can we prevent this? Turn off Shrinkage? Not Desirable.
          forAll (genListMeanAboveThreshold) { (n: List[(String, Double, Double)]) =>
            whenever (
              n.size > 20 &&
              n.exists {
                tup =>
                  tup._2 > 1 && tup._3 > 1 && tup._2 <= tup._3
              }
            ) {

              meanCostDiff(n) should equal (0)
            }
          }
        }
      }
    }
    "selectionMethod" when {
      // TODO: Gen fails to generate a valid solution?
      "called with a set of alternates and no children have been explored" ignore {
        "select one randomly" in new MCTSTestAssets.selectionMethodGenerator {
          forAll (globalAltsGen) { (globalAlts: Seq[(PersonID, Map[Tag, Seq[String]])]) =>
            whenever(
              globalAlts.nonEmpty &&
              globalAlts.forall(_._1.nonEmpty) &&
              globalAlts.forall(_._2.forall(_._2.nonEmpty))
            ){
              println(globalAlts.toMap)
              val selectionMethodTest: (GenMap[Tag, () => Option[MCTSTreeNode]]) => MCTSAltPath =
                MCTSUtilities(Some(1), congestionThreshold, globalAlts.toMap).selectionMethod
              val rootChildren: GenMap[Tag,() => Option[MCTSTreeNode]] =
                for {
                  person <- globalAlts.toMap.take(1)
                  alt <- person._2
                } yield (alt._1, () => None)
              selectionMethodTest(rootChildren) should equal (MCTSAltPath(Tag("a", 1), Seq("1")))
            }
          }
        }
      }
      "called with a set of alternates and no children have been explored" should {
        "select one randomly" in new MCTSTestAssets.selectionMethodMock {
          // This hard-coded version temporarily provides test coverage for selectionMethod
          // where the property testing version above is failing.
          // I had issues Generating a structured data type that doesn't break form
          // when shrinkage is applied.
          val selectionMethodTest: (GenMap[Tag, () => Option[MCTSTreeNode]]) => MCTSAltPath =
            MCTSUtilities(Some(1), congestionThreshold, globalAlts).selectionMethod
          val rootChildren: GenMap[Tag,() => Option[MCTSTreeNode]] =
            for {
              person <- globalAlts.take(1)
              alt <- person._2
            } yield (alt._1, () => None)
          selectionMethodTest(rootChildren) should equal (MCTSAltPath(Tag("e", 5), Seq("e-alt5-edge1", "e-alt5-edge2")))
        }
      }
    }
  }
}
