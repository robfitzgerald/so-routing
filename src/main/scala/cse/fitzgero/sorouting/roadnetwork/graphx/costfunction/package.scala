package cse.fitzgero.sorouting.roadnetwork.graphx

package object costfunction {
  type AttributesAndDefaults = Map[String, Double]

  /**
    * parses a map of attributes into a sequence of values for constructing some cost function object
    * @param attributes a map of attribute/value pairs taken from a file or elsewhere
    * @param expectedAttributes pairs of expected attribute names and default values for those attributes if missing
    * @return
    */
  def parseAttributes(attributes: Map[String, String], expectedAttributes: AttributesAndDefaults): List[Double] = {
    expectedAttributes.foldLeft(List.empty[Double])((accum, attr) => attributes.get(attr._1) match {
      case Some(value: String) => accum :+ value.toDouble
      case None => accum :+ attr._2
    })
  }


  case class CostFunctionAttributes (
    capacity: Double = 100D,
    freespeed: Double = 50D,
    flow: Double = 0D,
    flowRate: Double = 3600D,
    algorithmFlowRate: Double = 3600D) extends Serializable {}
}
