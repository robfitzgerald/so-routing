package cse.fitzgero.sorouting.algorithm.pathsearch.ksp

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath

import scala.collection.GenSeq

class KSPSearchTreeTests extends SORoutingUnitTestTemplate {
  "KSPSearchTree" when {
    "buildTree" when {
      "called with the output of a KSP algorithm" should {
        "produce a tree of shortest paths" in {
          val kspResult: GenSeq[LocalGraphODPath] = GenSeq(
            LocalGraphODPath(1L, 5L, List("a", "b", "c"), List(1.0, 1.0, 1.0)),
            LocalGraphODPath(1L, 5L, List("a", "d", "e", "f"), List(1.0, 1.0, 1.0, 1.0)),
            LocalGraphODPath(1L, 5L, List("b", "g", "n"), List(1.0, 1.0, 1.0))
          )
          val result = KSPSearchTree.buildTree[LocalGraphODPath, Long, String](kspResult)
          result match {
            case KSPSearchRoot(children, src, dst) =>
              children.head._1 should equal ("a")
              children.head._3 match {
                case KSPSearchBranch(ch) =>
                  ch.head._1 should equal ("b")
                  ch.tail.head._1 should equal ("d")
                case _ => fail()
              }
            case _ => fail()
          }
        }
      }
      "called with an empty result from a ksp algorithm" should {
        "produce a KSPEmptySearchTree" in {
          val kspResult = GenSeq.empty[LocalGraphODPath]
          val result = KSPSearchTree.buildTree[LocalGraphODPath, Long, String](kspResult)
          result should equal (KSPEmptySearchTree)
        }
      }
    }
    "discoverAlternatives" when {
      "called with a few ods" should {
        "find the different first steps in all alternates available" in {
          val kspResult: GenSeq[TreeBuildData[Long, String]] = GenSeq(
            TreeBuildData[Long, String](1L, 5L, List("a", "b", "c"), List(1.0, 1.0, 1.0)),
            TreeBuildData[Long, String](1L, 5L, List("a", "d", "e", "f"), List(1.0, 1.0, 1.0, 1.0)),
            TreeBuildData[Long, String](1L, 5L, List("b", "g", "n"), List(1.0, 1.0, 1.0)),
            TreeBuildData[Long, String](1L, 5L, List(), List())
          )
          val result = KSPSearchTree.discoverAlternatives[Long, String](kspResult)
          result should equal (GenSeq(("a", 1.0D), ("b", 1.0D)))
        }
      }
    }
    "groupAlternatesBy" when {
      "called with a few ods and an edge which exists at the head of at least one path" should {
        "return just the od path that matches" in {
          val kspResult: GenSeq[TreeBuildData[Long, String]] = GenSeq(
            TreeBuildData[Long, String](1L, 5L, List("m", "d", "e", "f"), List(1.0, 1.0, 1.0, 1.0)),
            TreeBuildData[Long, String](1L, 5L, List("a", "b", "c"), List(1.0, 1.0, 1.0)),
            TreeBuildData[Long, String](1L, 5L, List("f", "g", "n"), List(1.0, 1.0, 1.0)),
            TreeBuildData[Long, String](1L, 5L, List(), List())
          )
          val result = KSPSearchTree.filterAlternatesBy[Long, String]("m", kspResult)
          result.head should equal (kspResult.head)
          result.length should equal (1)
        }
      }
    }
    "stepIntoPaths" when {
      "called with a group of ods" should {
        "take a step into their paths and costs" in {
          val kspResult: GenSeq[TreeBuildData[Long, String]] = GenSeq(
            TreeBuildData[Long, String](1L, 5L, List("a", "d", "e", "f"), List(1.0, 1.0, 1.0, 1.0)),
            TreeBuildData[Long, String](1L, 5L, List("a", "b", "c"), List(1.0, 1.0, 1.0)),
            TreeBuildData[Long, String](1L, 5L, List("a", "g", "n"), List(1.0, 1.0, 1.0)),
            TreeBuildData[Long, String](1L, 5L, List("a"), List(1.0)),
            TreeBuildData[Long, String](1L, 5L, List(), List())
          )
          val result = KSPSearchTree.stepIntoPaths[Long, String](kspResult)
          result.foreach(println)
        }
      }
    }

  }
}