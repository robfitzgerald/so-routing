//package cse.fitzgero.sorouting.util
//
//import java.time.LocalTime
//import scala.util.{Failure, Success, Try}
//import scala.util.matching.Regex
//
//import cse.fitzgero.sorouting.algorithm.pathsearch.ksp.{KSPBounds, PathsFoundBounds, TimeBounds}
//import cse.fitzgero.sorouting.algorithm.trafficassignment.{IterationFWBounds, RelativeGapFWBounds, RunningTimeFWBounds, FWBounds}
//
//
//case class SORoutingApplicationConfig2 (
//  matsimConfigFile: String,
//  matsimNetworkFile: String,
//  workingDirectory: String,
//  numProcesses: Parallelism,
//  algorithmTimeWindow: String,
//  populationSize: Int,
//  routePercentage: Double,
//  startTime: String,   // HH:mm:ss
//  endTime: String,      // HH:mm:ss
//  k: Int,
//  kspBounds: KSPBounds,
//  fwBounds: FWBounds
//)
//
//object SORoutingApplicationConfigParseArgs2 {
//  def filePathArgsRegex(flag: String): Regex = s"-$flag ([\\w .\\/-]+)".r
//  def makeNumberArgsRegex(flag: String): Regex = s"-$flag (\\d+(?:\\.\\d*)?)".r
//  def makeTimeArgsRegex(flag: String): Regex = s"-$flag ([0-9]{2}:[0-9]{2}:[0-9]{2})".r
//  def makeNumProcsRegex(flag: String): Regex = s"-$flag (\\*|(?:\\d+(?:\\.\\d*)?)){1}".r
//
//  val confFile: Regex = filePathArgsRegex("conf")
//  val networkFile: Regex = filePathArgsRegex("network")
//  val workDir: Regex = filePathArgsRegex("wdir")
//  val numProcs: Regex = makeNumProcsRegex("procs")
//  val timeWindow: Regex = makeNumberArgsRegex("win")
//  val popSize: Regex = makeNumberArgsRegex("pop")
//  val routePercent: Regex = makeNumberArgsRegex("route")
//  val startTime: Regex = makeTimeArgsRegex("start")
//  val endTime: Regex = makeTimeArgsRegex("end")
//  val kValue: Regex = makeNumberArgsRegex("k")
//  val kspBounds: Regex = s"-kBounds (pathsfound|time) ([0-9]{2}:[0-9]{2}:[0-9]{2}|\\d+)".r
//  val fwBounds: Regex = s"-fwBounds (iteration|time|relgap) ([0-9]{2}:[0-9]{2}:[0-9]{2}|\\d+)".r
//
//  val Zero: Int = 0
//  val One: Int = 1
//  val MaxSecsPerDay: Int = 86400
//  val OneHundredPercent: Int = 100
//
//  def asPercent(a: Int): Double = a / 100.0D
//
//  def withNumberBounds(s: String, low: Int = Int.MinValue, high: Int = Int.MaxValue): Int =
//    Try({
//      s.toInt
//    }) match {
//      case Success(i) =>
//        if (i < low || high < i) throw new ArithmeticException(s"[$i] does not conform to the bounds [$low, $high]")
//        else i
//      case Failure(e) => throw new ArithmeticException(s"[$s] does not conform to type Int")
//    }
//
//  def validTime(s: String): String =
//    Try({
//      LocalTime.parse(s)
//    }) match {
//      case Success(_) => s
//      case Failure(e) => throw new IllegalArgumentException(s"[$s] is not a valid time in the range [00:00, 23:59]")
//    }
//
//  def numProcsType(s: String): Parallelism =
//    if (s == "*") AllProcs
//    else if (s == "1") OneProc
//    else NumProcs(s.toInt)
//
//  def kspBoundsType(s: String): KSPBounds = s.split(" ").head match {
//    case "pathsfound" => PathsFoundBounds(s.split(" ").last.toInt)
//    case "time" => TimeBounds(s.split(" ").last.toLong)
//    case _ => throw new IllegalArgumentException(s"cannot parse k-shortest paths bounds argument: $s")
//  }
//
//  def fwBoundsType(s: String): FWBounds = s.split(" ").head match {
//    case "iteration" => IterationFWBounds(s.split(" ").last.toInt)
//    case "time" => RunningTimeFWBounds(s.split(" ").last.toLong)
//    case "relgap" => RelativeGapFWBounds(s.split(" ").last.toLong)
//    case _ => throw new IllegalArgumentException(s"cannot parse frank wolfe bounds argument: $s")
//  }
//
//  def test(args: Array[String], size: Int)(r: Regex): String = {
//    val a = args.sliding(size).map(_.mkString(" "))
//    a.foldLeft(Option.empty[String])((acc, input) => input match {
//      case r(g0) => Some(g0.toString)
//      case r(g0, g1) => Some(s"$g0 $g1")
//      case _ => acc
//    }) match {
//      case Some(s) => s
//      case _ => ""
//    }
//  }
//
//  // TODO: test, wire in. what was once somewhat self-documenting is getting labrynthian. and what about default values?
//  def apply(args: Array[String]): SORoutingApplicationConfig2 = {
//    val oneKeyOneValue = test(args, 2)_
//    val oneKeyTwoValues = test(args, 3)_
//    SORoutingApplicationConfig2(
//      oneKeyOneValue(confFile),
//      oneKeyOneValue(networkFile),
//      oneKeyOneValue(workDir),
//      numProcsType(oneKeyOneValue(numProcs)),
//      withNumberBounds(oneKeyOneValue(timeWindow), Zero, MaxSecsPerDay).toString,
//      withNumberBounds(oneKeyOneValue(popSize), Zero),
//      asPercent(withNumberBounds(oneKeyOneValue(routePercent), Zero, OneHundredPercent)),
//      validTime(oneKeyOneValue(startTime)),
//      validTime(oneKeyOneValue(endTime)),
//      withNumberBounds(oneKeyOneValue(kValue), One),
//      kspBoundsType(oneKeyTwoValues(kspBounds)),
//      fwBoundsType(oneKeyTwoValues(fwBounds))
//    )
//  }
//}
