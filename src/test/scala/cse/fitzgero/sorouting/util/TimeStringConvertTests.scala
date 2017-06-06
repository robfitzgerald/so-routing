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
        val badTime: String = "-01:00:00"
        val thrown = the [java.lang.ArithmeticException] thrownBy TimeStringConvert.fromString(badTime)
        thrown.getMessage should equal (s"String $badTime does not conform to 24-hour clock specification - failed bounds test.")
      }
      "throw an error for out of upper bounds time strings" in {
        val badTime: String = "24:00:00"
        val thrown = the [java.lang.ArithmeticException] thrownBy TimeStringConvert.fromString(badTime)
        thrown.getMessage should equal (s"String $badTime does not conform to 24-hour clock specification - failed bounds test.")
      }
      "throw an error for values which are not parseable into integers" in {
        val badTime: String = "hh:00:00"
        val thrown = the [java.lang.ArithmeticException] thrownBy TimeStringConvert.fromString(badTime)
        thrown.getMessage should equal (s"String $badTime does not conform to 24-hour clock specification - values were not integers.")
      }
      "throw an error for values which cannot be broken into 3 parts by the : separator" in {
        val badTime: String = "01:30"
        val thrown = the [java.lang.ArithmeticException] thrownBy TimeStringConvert.fromString(badTime)
        thrown.getMessage should equal (s"String $badTime does not conform to 24-hour clock specification - failed partitioning.")
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
        val badTime: Int = -1
        val thrown = the [java.lang.ArithmeticException] thrownBy TimeStringConvert.fromInt(badTime)
        thrown.getMessage should equal (s"Int $badTime does not conform to 24-hour clock specification - failed bounds test [0, 86400).")
      }
      "throw an error for out of upper bounds time values" in {
        val badTime: Int = 86400
        val thrown = the [java.lang.ArithmeticException] thrownBy TimeStringConvert.fromInt(badTime)
        thrown.getMessage should equal (s"Int $badTime does not conform to 24-hour clock specification - failed bounds test [0, 86400).")
      }
    }
    "stringToInt" should {
      "correctly convert string values to int values" in {
        val stringToInt = PrivateMethod[Int]('stringToInt)
        TimeStringConvert invokePrivate stringToInt("00","00","00") should equal (0)
        TimeStringConvert invokePrivate stringToInt("06","00","00") should equal (21600)
        TimeStringConvert invokePrivate stringToInt("06","00","01") should equal (21601)
        TimeStringConvert invokePrivate stringToInt("06","01","00") should equal (21660)
        TimeStringConvert invokePrivate stringToInt("23","59","59") should equal (86399)
      }
    }
    "intToString" should {
      "correctly convert int values to string values" in {
        val intToString = PrivateMethod[String]('intToString)
        TimeStringConvert invokePrivate intToString(0) should equal ("00:00:00")
        TimeStringConvert invokePrivate intToString(21600) should equal ("06:00:00")
        TimeStringConvert invokePrivate intToString(21601) should equal ("06:00:01")
        TimeStringConvert invokePrivate intToString(21660) should equal ("06:01:00")
        TimeStringConvert invokePrivate intToString(86399) should equal ("23:59:59")
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
    "intInBounds" should {
      "identify values that are in range" in {
        val intInBounds = PrivateMethod[Boolean]('intInBounds)
        TimeStringConvert invokePrivate intInBounds(0) should equal (true)
        TimeStringConvert invokePrivate intInBounds(86399) should equal (true)
      }
      "identify values out of range" in {
        val intInBounds = PrivateMethod[Boolean]('intInBounds)
        TimeStringConvert invokePrivate intInBounds(-1) should equal (false)
        TimeStringConvert invokePrivate intInBounds(86400) should equal (false)
      }
    }
  }
}
