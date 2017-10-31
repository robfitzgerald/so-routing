#!/usr/bin/env bash
echo "populationSize,totalRouteRequests,routeRequestsUE,routeRequestsSO,routePercent,windowDuration,combinations,expectedUECostEffect,expectedSOCostEffect,popAvgTripUE,popAvgTripUESO,netAvgTripUE,netAvgTripUESO,popTravelTimeImprovement,netTravelTimeImprovement" >> result/20171020/result.csv
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 10 1.0"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 10 0.9"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 10 0.8"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 10 0.7"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 10 0.6"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 10 0.5"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 10 0.4"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 10 0.3"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 10 0.1"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 5"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 8"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 11"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 14"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 17"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 20"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 23"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 26"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314 30"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 100"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 100"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 100"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 100"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 500"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 500"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 500"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 500"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 1000"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 1000"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 1000"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 1000"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 2500"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 2500"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 2500"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 2500"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 5000"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 5000"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 5000"
#sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimTest 5000"