# System-Optimal Routing (SO Routing)

### A system for exploring equilibrium-based traffic routing in a near-time, big-data context

##### Installation

- install global dependencies
  - Java 8
  - [Scala Build Tool (SBT)](https://www.scala-sbt.org/)
  
- clone this repository:

```bash
$ git clone https://gitlab.com/ucdenver-bdlab/SO-Routing
$ cd SO-Routing
```

- install build dependencies

```bash
$ sbt
```

##### Run

The testbed currently runs via scripts which call specific drivers from SBT.

---

###### User Equilibrium Example

This runs a simulation of drivers using selfish routing. It has the following arguments:
- 2000 people
- 15 second windows for batch scheduling
- 0.20 aka 20% system optimal route percentage (only used for tagging this experiment's directory)
- 1.1 aka 10% congestion threshold (only used for tagging this experiment's directory)
- experiment name is "test"
- run using the Rye, Colorado network data

```bash
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimUETest 2000 15 0.20 1.1 test data/rye"
```

###### System Optimal Example

This runs a simulation of drivers where 20% are routed with a system-optimal approach.
- Takes the same arguments

```bash
sbt -mem 12288 "run-main cse.fitzgero.sorouting.experiments.MATSimSO_MCTSTest 2000 15 0.20 1.1 test data/rye"
```

###### Directory Structure

Running the above experiments would yield the following:

```
result/
  test/
    rye-2000-15-0.2-1.1/
      <time-of-experiment-1>/
        <data>
      <time-of-experiment-2>/
        <data>
    report.csv # aggregate report of all (2) experiments with name "test" and the same arguments
```

