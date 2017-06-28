package cse.fitzgero.sorouting.matsimrunner

package object population {
  trait PopulationDataThatConvertsToXml {
    def toXml: xml.Elem
  }
}
