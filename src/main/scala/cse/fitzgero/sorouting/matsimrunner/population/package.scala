package cse.fitzgero.sorouting.matsimrunner

import java.time.LocalTime

import cse.fitzgero.sorouting.roadnetwork.graphx.vertex.Euclidian
import cse.fitzgero.sorouting.roadnetwork.graphx.edge.EdgeIdType
import org.apache.spark.graphx.VertexId

package object population {
  trait ConvertsToXml {
    def toXml: xml.Elem
  }
  type PersonIDType = String
  type ActivityData = (VertexId, Euclidian, EdgeIdType)
  type ActivityLocations = Array[(EdgeIdType, Euclidian)]
  type ModeTargetTimeAndDeviation = Seq[(String, ActivityTime)]
  type GeneratedTimeValues = Map[String, LocalTime]
}