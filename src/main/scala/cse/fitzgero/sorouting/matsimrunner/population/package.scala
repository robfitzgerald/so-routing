package cse.fitzgero.sorouting.matsimrunner

import java.time.LocalTime

import cse.fitzgero.sorouting.roadnetwork.edge.EdgeIdType
import cse.fitzgero.sorouting.roadnetwork.vertex.Euclidian

package object population {
  trait ConvertsToXml {
    def toXml: xml.Elem
  }
  type PersonIDType = String
  type ActivityLocations = Array[(EdgeIdType, Euclidian)]
  type ModeTargetTimeAndDeviation = Seq[(String, ActivityTime)]
  type GeneratedTimeValues = Map[String, LocalTime]
}
