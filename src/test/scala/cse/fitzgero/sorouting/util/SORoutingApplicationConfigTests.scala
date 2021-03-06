package cse.fitzgero.sorouting.util

import java.time.LocalTime

import cse.fitzgero.sorouting.SORoutingUnitTestTemplate

class SORoutingApplicationConfigTests extends SORoutingUnitTestTemplate {
  "SORoutingParseArgs" when {
    "apply" when {
      "passed all the flags" should {
        "produce a config object" in {
          val args =
            Array(
              "-conf", "file.xml",
              "-network", "network.xml",
              "-wdir", "result/files",
              "-procs", "2",
              "-win", "10",
              "-pop", "5000",
              "-route", "100",
              "-start", "00:00:00",
              "-end", "23:59:00"
            )
          val result: SORoutingApplicationConfig = SORoutingApplicationConfigParseArgs(args)
          result should equal (
            SORoutingApplicationConfig
            (
              "file.xml",
              "network.xml",
              "result/files",
              "2",
              "10",
              5000,
              1.0,
              "00:00:00",
              "23:59:00"
            )
          )

        }
      }
    }
    "test" when {
      "passed args with valid flags" should {
        "find a configuration file" in {
          val args = Array("-conf", "filename","other", "flags")
          val result = SORoutingApplicationConfigParseArgs.test(args)(SORoutingApplicationConfigParseArgs.confFile)
          result should equal ("filename")
        }
        "find a working directory" in {
          val args = Array("-wdir", "working/directory","other", "flags")
          val result = SORoutingApplicationConfigParseArgs.test(args)(SORoutingApplicationConfigParseArgs.workDir)
          result should equal ("working/directory")
        }
        "find spark procs wildcard" in {
          val args = Array("-procs","*","other","flags")
          val result = SORoutingApplicationConfigParseArgs.test(args)(SORoutingApplicationConfigParseArgs.sparkProcs)
          result should equal ("*")
        }
        "find spark procs number" in {
          val args = Array("-procs","64","other","flags")
          val result = SORoutingApplicationConfigParseArgs.test(args)(SORoutingApplicationConfigParseArgs.sparkProcs)
          result should equal ("64")
        }
        "find time window" in {
          val args = Array("-win","5","other","flags")
          val result = SORoutingApplicationConfigParseArgs.test(args)(SORoutingApplicationConfigParseArgs.timeWindow)
          result should equal ("5")
        }
        "find population size" in {
          val args = Array("-pop","100000","other","flags")
          val result = SORoutingApplicationConfigParseArgs.test(args)(SORoutingApplicationConfigParseArgs.popSize)
          result should equal ("100000")
        }
        "find a start time" in {
          val args = Array("-start","06:00:00","other","flags")
          val result = SORoutingApplicationConfigParseArgs.test(args)(SORoutingApplicationConfigParseArgs.startTime)
          result should equal ("06:00:00")
        }
        "find a end time" in {
          val args = Array("-end","17:00:00","other","flags")
          val result = SORoutingApplicationConfigParseArgs.test(args)(SORoutingApplicationConfigParseArgs.endTime)
          result should equal ("17:00:00")
        }
      }
    }
  }
}
