package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType
import org.apache.spark.graphx.VertexId

sealed trait ActivityNodeOptions
final case class EndTime (endTime: LocalTime) extends ActivityNodeOptions
final case class Dur (dur: LocalTime) extends ActivityNodeOptions
final case class NoActivity () extends ActivityNodeOptions

sealed trait ActivityNode[Pos, VertexId, EdgeId, ActOpt <: ActivityNodeOptions] {
  def `type`: String
  def x: Pos
  def y: Pos
  def vertex: VertexId
  def link: EdgeId
  def opts: ActOpt
}

abstract class MatsimActivity extends ActivityNode[Double, VertexId, EdgeIdType, EndTime]

final case class MorningActivity (
  `type`: String,
  x: Double,
  y: Double,
  vertex: VertexId,
  link: EdgeIdType,
  opts: EndTime)
  extends MatsimActivity with ConvertsToXml {
    override def toXml: xml.Elem =
      <activity type={`type`} x={x.toString} y={y.toString} link={link.toString} end_time={opts.endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}/>
  }

final case class MiddayActivity (
  `type`: String,
  x: Double,
  y: Double,
  vertex: VertexId,
  link: EdgeIdType,
  opts: EndTime)
  extends MatsimActivity with ConvertsToXml {
    override def toXml: xml.Elem =
        <activity type={`type`} x={x.toString} y={y.toString} link={link.toString} end_time={opts.endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}/>
  }

final case class EveningActivity (
  `type`: String,
  x: Double,
  y: Double,
  vertex: VertexId,
  link: EdgeIdType,
  opts: NoActivity = NoActivity())
  extends ActivityNode[Double, VertexId, EdgeIdType, NoActivity] with ConvertsToXml {
    override def toXml: xml.Elem =
      <activity type={`type`} x={x.toString} y={y.toString} link={link.toString}/>
  }