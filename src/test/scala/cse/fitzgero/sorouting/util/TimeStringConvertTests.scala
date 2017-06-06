package cse.fitzgero.sorouting.util

import scala.util.{Try, Success, Failure}
import cse.fitzgero.sorouting.SORoutingUnitTests
import org.scalatest.PrivateMethodTester

class TimeStringConvertTests extends SORoutingUnitTests with PrivateMethodTester {
  "TimeStringConvert" when {
    "fromString" should {
      "correctly convert string time to int time" in {
        TimeStringConvert.fromString("00:00:00") should equal (0)
        TimeStringConvert.fromString("06:00:00") should equal (21600)
        TimeStringConvert.fromString("06:00:01") should equal (21601)
        TimeStringConvert.fromString("06:01:00") should equal (21660)
        TimeStringConvert.fromString("23:59:59") should equal (86399)
      }
      "throw an error for out of lower bounds time strings" in {
        val thrown = the [java.lang.ArithmeticException] thrownBy TimeStringConvert.fromString("-00:00:00")
        thrown.getMessage should equal ("something logical")
      }
      "throw an error for out of upper bounds time strings" in {
        val thrown = the [java.lang.ArithmeticException] thrownBy TimeStringConvert.fromString("24:00:00")
        thrown.getMessage should equal ("something logical")
      }
    }
    "fromInt" should {
      "correctly convert int time to string" in {
        TimeStringConvert.fromInt(0) should equal ("00:00:00")
        TimeStringConvert.fromInt(21600) should equal ("06:00:00")
        TimeStringConvert.fromInt(21601) should equal ("06:00:01")
        TimeStringConvert.fromInt(21660) should equal ("06:01:00")
        TimeStringConvert.fromInt(86399) should equal ("23:59:59")
      }
      "throw an error for out of lower bounds time values" in {
        val thrown = the [java.lang.ArithmeticException] thrownBy TimeStringConvert.fromInt(-1)
        thrown.getMessage should equal ("something logical")
      }
      "throw an error for out of upper bounds time values" in {
        val thrown = the [java.lang.ArithmeticException] thrownBy TimeStringConvert.fromInt(86400)
        thrown.getMessage should equal ("something logical")
      }
    }
    "stringToInt" should {
      "correctly covert string values to int values" in {
        val stringToInt = PrivateMethod[Int]('stringToInt)
        TimeStringConvert invokePrivate stringToInt("00","00","00") should equal (0)
        TimeStringConvert invokePrivate stringToInt("06","00","00") should equal (21600)
        TimeStringConvert invokePrivate stringToInt("06","00","01") should equal (21601)
        TimeStringConvert invokePrivate stringToInt("06","01","00") should equal (21660)
        TimeStringConvert invokePrivate stringToInt("23","59","59") should equal (86399)
      }
    }
    "stringsInBounds" should {
      "identify correctly formed strings" in {
        val stringsInBounds = PrivateMethod[Try[Boolean]]('stringsInBounds)
        TimeStringConvert invokePrivate stringsInBounds("00","00","00") should equal (Success(true))
        TimeStringConvert invokePrivate stringsInBounds("06","00","00") should equal (Success(true))
        TimeStringConvert invokePrivate stringsInBounds("06","00","01") should equal (Success(true))
        TimeStringConvert invokePrivate stringsInBounds("06","01","00") should equal (Success(true))
        TimeStringConvert invokePrivate stringsInBounds("23","59","59") should equal (Success(true))
      }
      "identify malformed strings" in {
        val stringsInBounds = PrivateMethod[Try[Boolean]]('stringsInBounds)
        TimeStringConvert invokePrivate stringsInBounds("24","00","00") should equal (Success(false))
        TimeStringConvert invokePrivate stringsInBounds("-2","00","00") should equal (Success(false))
        TimeStringConvert invokePrivate stringsInBounds("00","65","00") should equal (Success(false))
        TimeStringConvert invokePrivate stringsInBounds("00","00","100") should equal (Success(false))
      }
    }
  }
}
