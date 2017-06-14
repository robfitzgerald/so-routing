package cse.fitzgero.sorouting.matsimrunner.snapshot

import cse.fitzgero.sorouting.FileWriteSideEffectTestTemplate
import org.matsim.api.core.v01.{Id, Scenario}
import org.matsim.core.config.{Config, ConfigUtils}
import org.matsim.core.scenario.ScenarioUtils

import scala.collection.JavaConverters._
import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.xml.Elem

class NetworkStateCollectorE2ETests extends FileWriteSideEffectTestTemplate("NetworkStateCollectorE2ETests") {
  "NetworkStateCollector" when {

    val v1 = Id.createVehicleId(1)
    val v2 = Id.createVehicleId(2)
    val v3 = Id.createVehicleId(3)
    val l1 = Id.createLinkId(1)
    val l2 = Id.createLinkId(20)
    val smallNetwork = NetworkStateCollector().addDriver(l1, v1).addDriver(l1, v2).addDriver(l2, v3)
    val timeGroup: String = "00:00:00"
    val rootPath: String = testRootPath

    "toRawFile" should {
      "store a file with the current network state" in {
        val writerData: WriterData = WriterData(rootPath, 1, timeGroup)
        val result: Try[String] = NetworkStateCollector.toRawFile(writerData, smallNetwork)

        result match {
          case Success(fileName) =>
            val dataInFile: String = (for (line <- Source.fromFile(fileName).getLines()) yield line).mkString("\n")
            dataInFile should equal ("1 2\n20 1")
          case Failure(err) =>
            fail(err)
        }
      }
    }
    "toXMLFile" should {
      "store an xml file with the current network state" in {
        val writerData: WriterData = WriterData(rootPath, 2, timeGroup)
        val result: Try[String] = NetworkStateCollector.toXMLFile(writerData, smallNetwork)

        result match {
          case Success(fileName) =>
            val dataInFile: Elem = scala.xml.XML.loadFile(fileName)
            val dataMap: Map[String,String] = Map.empty[String, String] ++
              (for (link <- dataInFile \ "links" \ "link") yield {
                val attrs: Map[String,String] = link.attributes.asAttrMap
                (attrs("id"), attrs("flow"))
              })
            dataMap("1") should equal ("2")
            dataMap("20") should equal ("1")
          case Failure(err) =>
            println(s"$err error on result of $result")
            fail(err)
        }
      }
    }
    "apply(org.matsim.api.core.v01.network.getLinks())" should {
      "store all link numbers to the NetworkStateCollector with flows all zeroes" in {

        // this was needed to be an E2E test since we cannot create stubs of the MATSim type "Link"
        val config: Config = ConfigUtils.loadConfig("src/test/resources/NetworkStateCollectorE2ETests/config.xml")
        val scenario: Scenario = ScenarioUtils.loadScenario(config)
        val networkStateCollector = NetworkStateCollector(scenario.getNetwork.getLinks.asScala)
        val stateToString: Array[Array[String]] =
          networkStateCollector
            .toString
            .split("\n")
            .map(line=>line.split(" "))
        val links: Array[Int] = stateToString
            .map(tup=>tup(0).toInt)

        // test: all link ids should be present
        (1 to 23).forall(links.contains(_)) should equal (true)

        // test: all link flows should add to zero
        stateToString.map(tup=>tup(1).toInt).sum should equal (0)
      }
    }
  }
}
