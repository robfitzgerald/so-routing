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

class PopulationRandomTimeGenerator2 (acts: Seq[ActivityConfig2]) extends PopulationTimeGenerator {
  val activities: Seq[ActivityConfig2] =
    if (acts.nonEmpty && acts.last.name == acts.head.name)
      acts.dropRight(1)
    else
      activities

  val actsWithDeviations: ModeTargetTimeAndDeviation =
    activities.map(act => {
      (act.name, BidirectionalBoundedDeviation(act.endTime, act.dev))
    })

  def next(): GeneratedTimeValues =
    actsWithDeviations.map(activity => {
      (activity._1, activity._2.nextTime())
    }).sortBy(_._2).toMap
}

object PopulationRandomTimeGenerator2 {
  def apply(acts: Seq[ActivityConfig2]): PopulationRandomTimeGenerator2 = new PopulationRandomTimeGenerator2(acts)
}