package cse.fitzgero.sorouting.matsimrunner

import java.time.LocalTime
import scala.xml

import cse.fitzgero.sorouting.roadnetwork.graphx.vertex.Euclidian
import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType
import org.apache.spark.graphx.VertexId


package object population {

  trait ConvertsToXml {
    def toXml: xml.Elem
  }

  case class HomeConfig(name: String)
  case class ActivityConfig(name: String, start: LocalTime, dur: LocalTime, dev: Long = 0L)
  case class ModeConfig(name: String, probability: Double = 1.0D)

  type PersonIDType = String
  type ActivityData = (VertexId, Euclidian, EdgeIdType)
  type ActivityLocations = Array[(EdgeIdType, Euclidian)]
  type ModeTargetTimeAndDeviation = Seq[(String, ActivityTime)]
  type GeneratedTimeValues = Map[String, LocalTime]
}
