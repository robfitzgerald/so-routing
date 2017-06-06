package cse.fitzgero.sorouting.util

import scala.util.{Try, Success, Failure}

/**
  * provides conversion between strings of the form hh:mm:ss and a raw seconds integer representation
  */
object TimeStringConvert {
  val TimeUpperBound: Int = 86400
  val HourBase: Int = 24
  val HourStep: Int = 3600
  val MinStep: Int = 60
  val MinBase: Int = 60
  val SecBase: Int = 60
  val TimeLowerBound: Int = 0

  /**
    * converts a string time representation into an Int time value
    * @param s a string of the form hh:mm:ss
    * @return a number, [0, 86400)
    */
  def fromString(s: String): Int = s.split(":") match {
    case Array(hh: String,mm: String,ss: String) => stringsInBounds(hh,mm,ss) match {
      case Success(true) => stringToInt(hh, mm, ss)
      case Success(false) => throw new java.lang.ArithmeticException(s"String $s does not conform to 24-hour clock specification - failed bounds test.")
      case _ => throw new java.lang.ArithmeticException(s"String $s does not conform to 24-hour clock specification - values were not integers.")
    }
    case _ => throw new java.lang.ArithmeticException(s"String $s does not conform to 24-hour clock specification - failed partitioning.")
  }

  /**
    * converts an Int time value to a string time representation
    * @param i a number, [0, 86400)
    * @return a string of the form hh:mm:ss
    */
  def fromInt(i: Int): String = {
    if (intInBounds(i)) intToString(i)
    else throw new java.lang.ArithmeticException(s"Int $i does not conform to 24-hour clock specification - failed bounds test [0, 86400).")
  }

  /**
    * parses a time delta value
    * @param w a string to be parsed within the range [0, 86400]
    * @return the Int representation of {w}
    */
  def windowValue(w: String): Int = {
    Try({w.toInt}) match {
      case Success(x) =>
        if ((TimeLowerBound to TimeUpperBound).contains(x)) x
        else throw new java.lang.ArithmeticException(s"String $w does not conform to a valid time delta value - failed bounds test [0, 86400].")
      case Failure(x) =>
        throw new java.lang.IllegalArgumentException(s"String $w cannot be parsed into a number.")
    }
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
