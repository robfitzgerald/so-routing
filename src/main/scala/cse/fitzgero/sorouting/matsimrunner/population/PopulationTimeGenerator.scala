package cse.fitzgero.sorouting.matsimrunner.population

abstract class PopulationTimeGenerator {
  def next(): GeneratedTimeValues
}

case class PopulationRandomTimeGenerator (
  activitiesWithTime: ModeTargetTimeAndDeviation
) extends PopulationTimeGenerator {
  def next(): GeneratedTimeValues =
    activitiesWithTime.map(activity => {
      (activity._1, activity._2.nextTime())
    }).toMap
}