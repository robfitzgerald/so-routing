package cse.fitzgero.sorouting.matsimrunner

import java.time.LocalTime

import cse.fitzgero.sorouting.roadnetwork.vertex.Euclidian
import org.apache.spark.graphx.VertexId

package object population {
  trait ConvertsToXml {
    def toXml: xml.Elem
  }
  type PersonIDType = String
  type ActivityLocations = Array[(VertexId, Euclidian)]
  type ModeTargetTimeAndDeviation = Seq[(String, ActivityTime)]
  type GeneratedTimeValues = Map[String, LocalTime]
}
