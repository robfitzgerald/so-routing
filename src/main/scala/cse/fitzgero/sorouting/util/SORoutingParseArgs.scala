package cse.fitzgero.sorouting.util

import java.time.LocalTime

import scala.util.matching.Regex

case class SORoutingApplicationConfig(
  matsimConfigFile: String,
  workingDirectory: String,
  sparkProcesses: String,
  algorithmTimeWindow: Int,
  populationSize: Long,
  startTime: LocalTime,
  endTime: LocalTime
)

object SORoutingParseArgs {
  // """-""" + flag + """ ([\w .\/-]+)"""
  // s"-$flag ([\\w .\\/-]+)".r
  def makeQuotedArgsRegex(flag: String): Regex = "-conf ([\\w.\\/-]+)".r
  def makeNumberArgsRegex(flag: String): Regex = ("""(?:-""" + flag + """ )(\d+(?:\.\d*)?)""").r
  def makeTimeArgsRegex(flag: String): Regex = ("""(?:-""" + flag + """ )([0-9]{2}:[0-9]{2})""").r
  def makeSparkProcsRegex(flag: String): Regex = ("""(?:-""" + flag + """ )(\*|(?:\d+(?:\.\d*)?)){1}""").r

  val confFile: Regex = makeQuotedArgsRegex("conf")
  val workDir: Regex = makeQuotedArgsRegex("wdir")
  val sparkProcs: Regex = makeSparkProcsRegex("procs")
  val timeW: Regex = makeNumberArgsRegex("win")
  val popSize: Regex = makeNumberArgsRegex("pop")
  val startTime: Regex = makeTimeArgsRegex("start")
  val endTime: Regex = makeTimeArgsRegex("end")

  def test(args: Array[String])(r: Regex): String = {
    val a = args.mkString(" ")
    a match {
      case r(all, group1) => group1
      case _ => throw new IllegalArgumentException(s"no match in arguments for pattern ${r.toString} found in $a - ${r.findFirstMatchIn(a)}")
    }
//    val result = r.findAllIn(a).group(1)
//    if (result.nonEmpty) result
//    else throw new IllegalArgumentException(s"no match in arguments for pattern ${r.toString} found in $a")
  }

  def apply(args: Array[String]): SORoutingApplicationConfig = {
    val checkFor = test(args)_
    SORoutingApplicationConfig(
      checkFor(confFile),
      checkFor(workDir),
      checkFor(sparkProcs),
      checkFor(timeW).toInt,
      checkFor(popSize).toLong,
      LocalTime.parse(checkFor(startTime)),
      LocalTime.parse(checkFor(endTime))
    )
  }
}
