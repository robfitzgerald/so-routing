package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType

sealed trait ActivityNodeOptions
final case class EndTime (endTime: LocalTime) extends ActivityNodeOptions
final case class Dur (dur: LocalTime) extends ActivityNodeOptions
final case class NoActivity () extends ActivityNodeOptions

sealed trait ActivityNode[Pos, EdgeID, ActOpt <: ActivityNodeOptions] {
  def `type`: String
  def x: Pos
  def y: Pos
  def link: EdgeID
  def opts: ActOpt
}

final case class MorningActivity (
  `type`: String,
  x: Double,
  y: Double,
  link: EdgeIdType,
  opts: EndTime)
  extends ActivityNode[Double, EdgeIdType, EndTime] with ConvertsToXml {
    override def toXml: xml.Elem =
      <act type={`type`} x={x.toString} y={y.toString} link={link.toString} end_time={opts.endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}/>
  }

final case class MiddayActivity (
  `type`: String,
  x: Double,
  y: Double,
  link: EdgeIdType,
  opts: Dur)
  extends ActivityNode[Double, EdgeIdType, Dur] with ConvertsToXml {
    override def toXml: xml.Elem =
      <act type={`type`} x={x.toString} y={y.toString} link={link.toString} dur={opts.dur.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}/>
  }

final case class EveningActivity (
  `type`: String,
  x: Double,
  y: Double,
  link: EdgeIdType,
  opts: NoActivity = NoActivity())
  extends ActivityNode[Double, EdgeIdType, NoActivity] with ConvertsToXml {
    override def toXml: xml.Elem =
      <act type={`type`} x={x.toString} y={y.toString} link={link.toString}/>
  }