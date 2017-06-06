package cse.fitzgero.sorouting.util

import scala.util.{Try, Success, Failure}

object TimeStringConvert {
  def fromString(s: String): Int = s.split(":") match {
    case Array(hh: String,mm: String,ss: String) => stringsInBounds(hh,mm,ss) match {
      case Success(true) => stringToInt(hh, mm, ss)
      case _ => throw new java.lang.ArithmeticException(s"String $s does not conform to 24-hour clock specification - failed bounds test.")
    }
    case _ => throw new java.lang.ArithmeticException(s"String $s does not conform to 24-hour clock specification - failed partitioning.")
  }

  def fromInt(i: Int): String = {
    ""
  }

  private def stringToInt(hh: String, mm: String, ss: String): Int = {
    (hh.toInt * 3600) + (mm.toInt * 60) + ss.toInt
  }

  private def stringsInBounds (hh: String, mm: String, ss: String): Try[Boolean] = {
    Try({
      0 <= hh.toInt && hh.toInt < 24 &&
      0 <= mm.toInt && mm.toInt < 60 &&
      0 <= ss.toInt && ss.toInt < 60
    })
  }
}
