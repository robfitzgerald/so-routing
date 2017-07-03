package cse.fitzgero.sorouting.util

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class SORoutingParseArgsTests extends SORoutingUnitTestTemplate {
  "SORoutingParseArgs" when {
    "test" when {
      "passed args with valid flags" should {
        "find a configuration file" in {
          val args = Array("-conf filename","other flags")
          val result = SORoutingParseArgs.test(args)(SORoutingParseArgs.confFile)
          result should equal ("filename")
        }
        "find a working directory" in {
          val args = Array("-wdir \"working/directory\"","other flags")
          val result = SORoutingParseArgs.test(args)(SORoutingParseArgs.workDir)
          result should equal ("working/directory")
        }
        "find spark procs wildcard" in {
          val args = Array("-procs *","other flags")
          val result = SORoutingParseArgs.test(args)(SORoutingParseArgs.sparkProcs)
          result should equal ("*")
        }
        "find spark procs number" in {
          val args = Array("-procs 64","other flags")
          val result = SORoutingParseArgs.test(args)(SORoutingParseArgs.sparkProcs)
          result should equal ("64")
        }
        "find time window" in {}
        "find population size" in {}
        "find a start time" in {}
        "find a end time" in {}
      }
    }
  }
}
