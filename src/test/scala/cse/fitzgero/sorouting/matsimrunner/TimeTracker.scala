package cse.fitzgero.sorouting.matsimrunner

import cse.fitzgero.sorouting.util.TimeStringConvert

/**
  * tracks the current time delta for use with negotiating incoming new link events
  * @param windowDuration time delta value used to determine the range of times within this time window
  * @param currentTimeWindowLowerBound the start time of the current time window, and name of the timeGroup
  * @param simulationEndTime the time exclusive where recording will cease
  */
class TimeTracker (
  private val windowDuration: Int,
  private val currentTimeWindowLowerBound: Int,
  private val simulationEndTime: Int) {
  /**
    * accepts string arguments which it tests for correctness before conversion
    * @param wD time delta value used to determine the range of times within this time window
    * @param startTimeString the start time of the current time window, and name of the timeGroup
    * @param endTimeString the time exclusive where recording will cease
    */
  def this (wD: String, startTimeString: String, endTimeString: String) {
    this(
      TimeStringConvert.windowValue(wD),
      TimeStringConvert.fromString(startTimeString),
      TimeStringConvert.fromString(endTimeString)
    )
  }
  // private members
  private val currentTimeWindowUpperBound: Int = currentTimeWindowLowerBound + windowDuration
  private val belongs: (Int) => Boolean = (x: Int) => {
    currentTimeWindowLowerBound until currentTimeWindowUpperBound contains x
  }

  /**
    * test if a given event falls within the range [currentTimeWindowLowerBound, currentTimeWindowLowerBound + windowDuration)
    * @param e a link enter or link leave event from MATSim
    * @return
    */
  def belongsToThisTimeGroup(e: SnapshotEventData): Boolean = {
    belongs(e.time)
  }

  /**
    * get the label of the current time group
    * @return
    */
  def currentTimeGroup: Int = currentTimeWindowLowerBound

  /**
    * get the label of the current time group
    * @return
    */
  def currentTimeString: String = TimeStringConvert.fromInt(currentTimeWindowLowerBound)

  /**
    * advance the time group by a time step the magnitude of the time delta value, windowDuration
    * @return
    */
  def advance: TimeTracker = TimeTracker(windowDuration, currentTimeWindowUpperBound, simulationEndTime)

  /**
    * tests if the current time window's start time exceeds the user-determined recording end time
    * @return
    */
  def isDone: Boolean = currentTimeWindowLowerBound >= simulationEndTime
}

object TimeTracker {
  def apply(wD: String, sT: String, eT: String): TimeTracker = new TimeTracker(wD, sT, eT)
  def apply(wD: Int, sT: Int, eT: Int): TimeTracker = new TimeTracker(wD, sT, eT)
}