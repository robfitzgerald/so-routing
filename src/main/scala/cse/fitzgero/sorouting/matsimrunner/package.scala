package cse.fitzgero.sorouting

package object matsimrunner {
  val ArgsMissingValues = true
  val ArgsNotMissingValues = false
  case class MATSimRunnerConfig(
    matsimConfigFile: String = "",
    outputDirectory: String = "",
    window: Int = 0,
    startTime: String = "",
    endTime: String = "",
    incomplete: Boolean = ArgsMissingValues)
}
