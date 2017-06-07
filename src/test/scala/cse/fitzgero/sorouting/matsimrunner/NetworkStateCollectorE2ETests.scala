package cse.fitzgero.sorouting.matsimrunner

import cse.fitzgero.sorouting.{FileWriteSideEffectTests, SORoutingUnitTests}
import cse.fitzgero.sorouting.app.SORoutingApplication
import org.matsim.api.core.v01.Id

import scala.io.Source
import scala.util.{Failure, Success, Try}

/**
  * Created by robertfitzgerald on 6/7/17.
  */
class NetworkStateCollectorE2ETests extends FileWriteSideEffectTests(testName = "NetworkStateCollectorE2ETests") {
  "NetworkStateCollector" when {
    "toFile" should {
      "store a file with the current network state" in {
        val network = NetworkStateCollector()
        val v1 = Id.createVehicleId(1)
        val v2 = Id.createVehicleId(2)
        val v3 = Id.createVehicleId(3)
        val l1 = Id.createLinkId(1)
        val l2 = Id.createLinkId(20)
        val smallNetwork = network.addDriver(l1, v1).addDriver(l1, v2).addDriver(l2, v3)

        val timeGroup: String = "00:00:00"
        val rootPath: String = s"test/temp/$testName"
        val directoryPath: String = "/1/"
        val name: String = s"snapshot-$timeGroup"
        val extension: String = ".nscData"
        NetworkStateCollector.toFile(rootPath, 1, timeGroup, smallNetwork)

        val fileName: String = rootPath + directoryPath + name + extension
        val dataInFile: String = (for (line <- Source.fromFile(fileName).getLines()) yield line).mkString("\n")
        dataInFile should equal ("1 2\n20 1")
      }
    }
  }
}
