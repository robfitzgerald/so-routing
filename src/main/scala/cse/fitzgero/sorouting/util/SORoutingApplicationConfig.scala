package cse.fitzgero.sorouting.util

import java.time.LocalTime

import cse.fitzgero.sorouting.app.SOAppConfig

import scala.util.{Failure, Success, Try}
import scala.util.matching.Regex

case class SORoutingApplicationConfig (
  matsimConfigFile: String,
  matsimNetworkFile: String,
  workingDirectory: String,
  sparkProcesses: String,
  algorithmTimeWindow: String,
  populationSize: Int,
  routePercentage: Double,
  startTime: String,   // HH:mm:ss
  endTime: String      // HH:mm:ss
) extends SOAppConfig

object SORoutingApplicationConfigParseArgs {
  def filePathArgsRegex(flag: String): Regex = s"-$flag ([\\w .\\/-]+)".r
  def makeNumberArgsRegex(flag: String): Regex = s"-$flag (\\d+(?:\\.\\d*)?)".r
  def makeTimeArgsRegex(flag: String): Regex = s"-$flag ([0-9]{2}:[0-9]{2}:[0-9]{2})".r
  def makeSparkProcsRegex(flag: String): Regex = s"-$flag (\\*|(?:\\d+(?:\\.\\d*)?)){1}".r

  val confFile: Regex = filePathArgsRegex("conf")
  val networkFile: Regex = filePathArgsRegex("network")
  val workDir: Regex = filePathArgsRegex("wdir")
  val sparkProcs: Regex = makeSparkProcsRegex("procs")
  val timeWindow: Regex = makeNumberArgsRegex("win")
  val popSize: Regex = makeNumberArgsRegex("pop")
  val routePercent: Regex = makeNumberArgsRegex("route")
  val startTime: Regex = makeTimeArgsRegex("start")
  val endTime: Regex = makeTimeArgsRegex("end")

  val Zero: Int = 0
  val MaxSecsPerDay: Int = 86400
  val OneHundredPercent: Int = 100
  def asPercent(a: Int): Double = a / 100.0D
  def withNumberBounds(s: String, low: Int = Int.MinValue, high: Int = Int.MaxValue): Int =
    Try({
      s.toInt
    }) match {
      case Success(i) =>
        if (i < low || high < i) throw new ArithmeticException(s"[$i] does not conform to the bounds [$low, $high]")
        else i
      case Failure(e) => throw new ArithmeticException(s"[$s] does not conform to type Int")
    }
  def validTime(s: String): String =
    Try({
      LocalTime.parse(s)
    }) match {
      case Success(_) => s
      case Failure(e) => throw new IllegalArgumentException(s"[$s] is not a valid time in the range [00:00, 23:59]")
    }

  def test(args: Array[String])(r: Regex): String = {
    val a = args.sliding(2).map(_.mkString(" "))
    a.foldLeft(Option.empty[String])((acc, input) => input match {
      case r(g0) => Some(g0.toString)
      case _ => acc
    }) match {
      case Some(s) => s
      case _ => ""
    }
  }

  def apply(args: Array[String]): SORoutingApplicationConfig = {
    val checkFor = test(args)_
    SORoutingApplicationConfig(
      checkFor(confFile),
      checkFor(networkFile),
      checkFor(workDir),
      checkFor(sparkProcs),
      withNumberBounds(checkFor(timeWindow), Zero, MaxSecsPerDay).toString,
      withNumberBounds(checkFor(popSize), Zero),
      asPercent(withNumberBounds(checkFor(routePercent), Zero, OneHundredPercent)),
      validTime(checkFor(startTime)),
      validTime(checkFor(endTime))
    )
  }
}
