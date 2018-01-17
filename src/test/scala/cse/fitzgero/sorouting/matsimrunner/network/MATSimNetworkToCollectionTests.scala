package cse.fitzgero.sorouting.matsimrunner.network

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

import scala.xml.XML

class MATSimNetworkToCollectionTests extends SORoutingUnitTestTemplate {
  "MATSimNetworkToCollection" when {
    val networkPath = "src/test/resources/SORoutingFilesHelperTests/network.xml"
    "called with a network file path" should {
      "create a network collection from that data" in {
        val network: Network = MATSimNetworkToCollection(networkPath)
        network.nodes.size should be (15)
        network.links.size should be (23)
      }
    }
    "called with a network xml element" should {
      "create a network collection from that data" in {
        val element = XML.load(new java.io.InputStreamReader(new java.io.FileInputStream(networkPath), "UTF-8"))
        val network: Network = MATSimNetworkToCollection(element)
        network.nodes.size should be (15)
        network.links.size should be (23)
      }
    }
  }
}
