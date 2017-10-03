package cse.fitzgero.sorouting.util

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class MLoggerTests extends SORoutingUnitTestTemplate {
  "MLogger" when {
    "AnalyticLogger" when {
      "a log is stored at a key" should {
        "be retrievable" in {
          val key = "test"
          val value = 300D
          AnalyticLogger()
            .update(key, value)
            .get(key) match {
            case Some(v) => v should equal (value)
            case None => fail()
          }
        }
      }
      "two logs combined" should {
        "add the values found at any shared keys" in {
          val (va1, va2, vb1, vb2) = (0, 100, 1000, 10000)
          val log1 = AnalyticLogger().update("a", va1).update("b", vb1)
          val log2 = AnalyticLogger().update("a", va2).update("b", vb2)
          val combined = log1 ++ log2
          combined.get("a") match {
            case Some(v) => v should equal (va1 + va2)
            case None => fail()
          }
          combined.get("b") match {
            case Some(v) => v should equal (vb1 + vb2)
            case None => fail()
          }
        }
        "include values that are at keys that are not present in both logs" in {
          val (va1, vb2, vc1, vd2) = (0, 100, 1000, 10000)
          val log1 = AnalyticLogger().update("a", va1).update("c", vc1)
          val log2 = AnalyticLogger().update("b", vb2).update("d", vd2)
          val combined = log1 ++ log2
          combined.get("a") match {
            case Some(v) => v should equal (va1)
            case None => fail()
          }
          combined.get("b") match {
            case Some(v) => v should equal (vb2)
            case None => fail()
          }
          combined.get("c") match {
            case Some(v) => v should equal (vc1)
            case None => fail()
          }
          combined.get("d") match {
            case Some(v) => v should equal (vd2)
            case None => fail()
          }
        }
      }
    }
    "RunTimeLogger" when {
      "a log is stored at a key" should {
        "be retrievable" in {
          val key = "test"
          val value = List(1000L)
          RunTimeLogger()
            .update(key, value)
            .get(key) match {
            case Some(v) => v should equal (value)
            case None => fail()
          }
        }
      }
      "two logs combined" should {
        "add the values found at any shared keys" in {
          val (va1, va2, vb1, vb2) = (List(0L), List(1L,2L,3L), List(10L,100L,1000L), List(4L,5L,6L))
          val log1 = RunTimeLogger().update("a", va1).update("b", vb1)
          val log2 = RunTimeLogger().update("a", va2).update("b", vb2)
          val combined = log1 ++ log2
          combined.get("a") match {
            case Some(v) => v should equal (va1 ::: va2)
            case None => fail()
          }
          combined.get("b") match {
            case Some(v) => v should equal (vb1 ::: vb2)
            case None => fail()
          }
        }
        "include values that are at keys that are not present in both logs" in {
          val (va1, vb2, vc1, vd2) = (List(0L), List(1L,2L,3L), List(10L,100L,1000L), List(4L,5L,6L))
          val log1 = RunTimeLogger().update("a", va1).update("c", vc1)
          val log2 = RunTimeLogger().update("b", vb2).update("d", vd2)
          val combined = log1 ++ log2
          combined.get("a") match {
            case Some(v) => v should equal (va1)
            case None => fail()
          }
          combined.get("b") match {
            case Some(v) => v should equal (vb2)
            case None => fail()
          }
          combined.get("c") match {
            case Some(v) => v should equal (vc1)
            case None => fail()
          }
          combined.get("d") match {
            case Some(v) => v should equal (vd2)
            case None => fail()
          }
        }
      }
    }
  }
}
