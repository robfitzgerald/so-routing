package cse.fitzgero.sorouting.matsimrunner.population

import scala.util.matching.Regex

sealed trait ActivityOptions
final case class EndTime (endTime: String) extends ActivityOptions
final case class Dur (dur: String) extends ActivityOptions
case object NoOptions extends ActivityOptions

case class ActivityNode (
  `type`: String,
  x: String,
  y: String,
  link: String,
  opts: ActivityOptions = NoOptions) extends PopulationDataThatConvertsToXml {
  decimalPattern.findFirstIn(x) match {
    case None => throw new IllegalArgumentException(s"$x does not match the pattern ${decimalPattern.toString}")
    case _ =>
  }
  decimalPattern.findFirstIn(y) match {
    case None => throw new IllegalArgumentException(s"$y does not match the pattern ${decimalPattern.toString}")
    case _ =>
  }
  integerPattern.findFirstIn(link) match {
    case None => throw new IllegalArgumentException(s"$x does not match the pattern ${integerPattern.toString}")
    case _ =>
  }
  override def toXml: xml.Elem = opts match {
    case EndTime(endTime) => <act type={`type`} x={x} y={y} link={link} end_time={endTime}/>
    case Dur(dur) => <act type={`type`} x={x} y={y} link={link} dur={dur}/>
    case NoOptions => <act type={`type`} x={x} y={y} link={link}/>
  }
}