package cse.fitzgero.sorouting.util

import scala.util.{Try, Success, Failure}

object TimeStringConvert {
  val TimeUpperBound: Int = 86400
  val HourBase: Int = 24
  val HourStep: Int = 3600
  val MinStep: Int = 60
  val MinBase: Int = 60
  val SecBase: Int = 60
  val TimeLowerBound: Int = 0

  def fromString(s: String): Int = s.split(":") match {
    case Array(hh: String,mm: String,ss: String) => stringsInBounds(hh,mm,ss) match {
      case Success(true) => stringToInt(hh, mm, ss)
      case Success(false) => throw new java.lang.ArithmeticException(s"String $s does not conform to 24-hour clock specification - failed bounds test.")
      case _ => throw new java.lang.ArithmeticException(s"String $s does not conform to 24-hour clock specification - values were not integers.")
    }
    case _ => throw new java.lang.ArithmeticException(s"String $s does not conform to 24-hour clock specification - failed partitioning.")
  }

  def fromInt(i: Int): String = {
    if (intInBounds(i)) intToString(i)
    else throw new java.lang.ArithmeticException(s"Int $i does not conform to 24-hour clock specification - failed bounds test [0, 86400).")
  }

  private def intToString(i: Int): String = {
    val hhVal: String = (i / HourStep).toString
    val mmVal: String = ((i % HourStep) / MinStep).toString
    val ssVal: String = (i % MinStep).toString
    val hh: String = if (hhVal.length == 1) "0" + hhVal else hhVal
    val mm: String = if (mmVal.length == 1) "0" + mmVal else mmVal
    val ss: String = if (ssVal.length == 1) "0" + ssVal else ssVal
    s"$hh:$mm:$ss"
  }

  private def stringToInt(hh: String, mm: String, ss: String): Int = {
    (hh.toInt * HourStep) + (mm.toInt * MinStep) + ss.toInt
  }

  private def stringsInBounds (hh: String, mm: String, ss: String): Try[Boolean] = {
    Try({
      TimeLowerBound <= hh.toInt && hh.toInt < HourBase &&
      TimeLowerBound <= mm.toInt && mm.toInt < MinBase &&
      TimeLowerBound <= ss.toInt && ss.toInt < SecBase
    })
  }

  private def intInBounds (i: Int): Boolean = (TimeLowerBound until TimeUpperBound).contains(i)
}
