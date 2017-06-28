package cse.fitzgero.sorouting.matsimrunner

import scala.util.matching.Regex

package object population {
  trait PopulationDataThatConvertsToXml {
    def toXml: xml.Elem
  }
  val decimalPattern: Regex = "^([0-9]+(?:.[0-9]*)?)$".r
  val integerPattern: Regex = "^([0-9]+)$".r
  val mmssPattern: Regex = "^([0-9]{2}:[0-9]{2})$".r
  val hhmmssPattern: Regex = "^([0-9]{2}:[0-9]{2}:[0-9]{2})$".r
}
