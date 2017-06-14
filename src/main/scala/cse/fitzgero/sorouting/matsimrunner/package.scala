package cse.fitzgero.sorouting

package object matsimrunner {
  val ArgsMissingValues = true
  val ArgsNotMissingValues = false
  case class AppConfig(inputDirectory: String = "", outputDirectory: String = "", window: String = "", startTime: String = "", endTime: String = "", incomplete: Boolean = ArgsMissingValues)
}
