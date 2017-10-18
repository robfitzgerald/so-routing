#!/usr/bin/env bash
echo "populationSize,possibleRouteRequests,routesUERequested,routesSORequested,routePercent,windowDuration,popAvgTripUE,popAvgTripUESO,netAvgTripUE,netAvgTripUESO,popTravelTimeImprovement,netTravelTimeImprovement" >> result/foo/result.csv
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 157"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 157"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 314"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 628"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 628"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 1256"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 1256"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 2502"
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.SOExperimentRefactor 2502"
