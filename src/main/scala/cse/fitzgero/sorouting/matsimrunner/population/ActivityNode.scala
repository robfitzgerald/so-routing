package cse.fitzgero.sorouting.matsimrunner.population

import java.time.LocalTime
import java.time.format.DateTimeFormatter

import org.apache.spark.graphx.VertexId

sealed trait ActivityNodeOptions
final case class EndTime (endTime: LocalTime) extends ActivityNodeOptions
final case class Dur (dur: LocalTime) extends ActivityNodeOptions
final case class NoActivity () extends ActivityNodeOptions

sealed trait ActivityNode[Pos, VertexId, ActOpt <: ActivityNodeOptions] {
  def `type`: String
  def x: Pos
  def y: Pos
  def vertex: VertexId
  def opts: ActOpt
}

final case class MorningActivity (
  `type`: String,
  x: Double,
  y: Double,
  vertex: VertexId,
  opts: EndTime)
  extends ActivityNode[Double, VertexId, EndTime] with ConvertsToXml {
    override def toXml: xml.Elem =
      <act type={`type`} x={x.toString} y={y.toString} link={vertex.toString} end_time={opts.endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}/>
  }

final case class MiddayActivity (
  `type`: String,
  x: Double,
  y: Double,
  vertex: VertexId,
  opts: ActivityNodeOptions)
  extends ActivityNode[Double, VertexId, ActivityNodeOptions] with ConvertsToXml {
    override def toXml: xml.Elem = opts match {
      case EndTime(endTime) => <act type={`type`} x={x.toString} y={y.toString} link={vertex.toString} end_time={endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}/>
      case Dur(dur) => <act type={`type`} x={x.toString} y={y.toString} link={vertex.toString} dur={dur.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}/>
      case _ => <act type={`type`} x={x.toString} y={y.toString} link={vertex.toString}/>
    }
  }

final case class EveningActivity (
  `type`: String,
  x: Double,
  y: Double,
  vertex: VertexId,
  opts: NoActivity = NoActivity())
  extends ActivityNode[Double, VertexId, NoActivity] with ConvertsToXml {
    override def toXml: xml.Elem =
      <act type={`type`} x={x.toString} y={y.toString} link={vertex.toString}/>
  }