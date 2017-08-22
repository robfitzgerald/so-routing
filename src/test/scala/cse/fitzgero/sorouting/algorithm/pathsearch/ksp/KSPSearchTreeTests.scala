package cse.fitzgero.sorouting.algorithm.pathsearch.ksp

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate
import cse.fitzgero.sorouting.algorithm.pathsearch.ksp
import cse.fitzgero.sorouting.algorithm.pathsearch.od.localgraph.LocalGraphODPath

import scala.collection.GenSeq

class KSPSearchTreeTests extends SORoutingUnitTestTemplate {
  "KSPSearchTree" when {
    "buildTree" when {
      "called with the output of a KSP algorithm" should {
        "produce a tree of shortest paths" in {
          val kspResult: GenSeq[LocalGraphODPath] = GenSeq(
            LocalGraphODPath("bob", 1L, 5L, List("a", "b", "c"), List(1.0, 1.0, 1.0)),
            LocalGraphODPath("alice", 1L, 5L, List("a", "d", "e", "f"), List(1.0, 1.0, 1.0, 1.0)),
            LocalGraphODPath("lord byron", 1L, 5L, List("b", "g", "n"), List(1.0, 1.0, 1.0))
          )
          val result = KSPSearchTree.buildTree[LocalGraphODPath, Long, String](kspResult)
          result match {
            case KSPSearchRoot(ch1, src, dst, id) =>
              ch1("a")._2 match {
                case KSPSearchBranch(ch2, _) =>
                  ch2("b")._2 match {
                    case KSPSearchBranch(ch3, branchDepth) =>
                      ch3("c")._2 should equal (KSPSearchLeaf)
                      branchDepth should equal (2)
                      // leaf depth is 3
                    case _ => fail()
                  }
                  ch2("d")._2 match {
                    case KSPSearchBranch(ch3, _) =>
                      ch3("e")._2 match {
                        case KSPSearchBranch(ch4, branchDepth) =>
                          ch4("f")._2 should equal (KSPSearchLeaf)
                          branchDepth should equal (3)
                        case _ => fail()
                      }
                    case _ => fail()
                  }
                case _ => fail()
              }
              ch1("b")._2 match {
                case KSPSearchBranch(ch2, _) =>
                  ch2("g")._2 match {
                    case KSPSearchBranch(ch3, branchDepth) =>
                      ch3("n")._2 should equal (KSPSearchLeaf)
                      branchDepth should equal (2)
                    case _ => fail()
                  }
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
    "traverse" when {
      "called with an edge that exists" should {
        "return the child node of that edge" in {
          val kspResult: GenSeq[LocalGraphODPath] = GenSeq(
            LocalGraphODPath("carlo", 1L, 5L, List("a", "b", "c"), List(1.0, 1.0, 1.0)),
            LocalGraphODPath("jerry", 1L, 5L, List("a", "d", "e", "f"), List(1.0, 1.0, 1.0, 1.0)),
            LocalGraphODPath("jeremy", 1L, 5L, List("b", "g", "n"), List(1.0, 1.0, 1.0))
          )
          KSPSearchTree.buildTree[LocalGraphODPath, Long, String](kspResult) match {
            case x: KSPSearchRoot[Long, String] =>
              x.traverse("a") match {
                case y: KSPSearchBranch[String] =>
                  y.traverse("b") match {
                    case z: KSPSearchBranch[String] =>
                      z.traverse("c") match {
                        case KSPSearchLeaf => succeed
                        case _ => fail()
                      }
                    case _ => fail()
                  }
                case _ => fail()
              }
            case _ => fail()
          }

        }
      }
      "called with an edge that doesn't exist" should {
        "return a KSPInvalidNode response" in {
          val kspResult: GenSeq[LocalGraphODPath] = GenSeq(
            LocalGraphODPath("A", 1L, 5L, List("a", "b", "c"), List(1.0, 1.0, 1.0)),
            LocalGraphODPath("B", 1L, 5L, List("a", "d", "e", "f"), List(1.0, 1.0, 1.0, 1.0)),
            LocalGraphODPath("C", 1L, 5L, List("b", "g", "n"), List(1.0, 1.0, 1.0))
          )
          val result = KSPSearchTree.buildTree[LocalGraphODPath, Long, String](kspResult)
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