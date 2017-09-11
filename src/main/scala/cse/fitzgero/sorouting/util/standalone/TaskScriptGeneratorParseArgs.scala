package cse.fitzgero.sorouting.util.standalone

import org.rogach.scallop._

object TaskScriptGeneratorParseArgs {

  val EmptyIterable = List("")
  val KSPOptions = Seq("time","pathsfound")
  val FWOptions = Seq("time","iteration","relgap")

  class Conf(args: Seq[String]) extends ScallopConf(args) {
    // experimentName: String (perhaps aggregated config vals),
    // timeWindow: Range | Int, soRouted: Range | Int, popSize: Range | Int
    //-conf data/rye/config.xml -network data/rye/network.xml -wdir result/$name -procs * -win $win -pop $pop -route $route -start 08:00:00 -end 18:00:00

    val name = opt[String](default = Some("test"), descr = "experiment name, defaults to 'test'")
    val algorithm = opt[Int](default = Some(1), descr = "the id of the routing algorithm to use (see cse.fitzgero.sorouting.app")
    val config = opt[String](descr = "MATSim config.xml file")
    val network = opt[String](descr = "MATSim network.xml file")
    val dest = opt[String](default = Some("result"), descr = "destination base directory. will store in $dest/$name")
    val repeat = opt[Int](default = Some(1), descr = "number of times to repeat these tests. default is no repetition.")
    val pop = propsLong[Double]("pop", keyName = "p", descr = "population size. if one value, a constant. if two values, a range. if three values, a range with a step factor.")
    val win = propsLong[Int]("win", keyName = "w", descr = "algorithm batch window duration, in seconds. if one value, a constant. if two values, a range. if three values, a range with a step value.")
    val route = propsLong[Double]("route", keyName = "r", descr = "% of population to route using our routing algorithm. if one value, a constant. if two values, a range. if three values, a range with a step value.")

    val klow = opt[Int](required = false, descr = "lower bound for k")
    val khigh = opt[Int](required = false, descr = "upper bound for k")
    val kstep = opt[Int](required = false, descr = "step size")
    val ksptype = opt[String](required = false, validate = (t) => KSPOptions.contains(t), descr = s"type of ksp bound ${KSPOptions.mkString("{","|","}")}")
    val kblow = opt[Int](required = false, descr = "lower bound for ksp, as # paths found, or time (seconds).")
    val kbhigh = opt[Int](required = false, descr = "upper bound for ksp, as # paths found, or time (seconds).")
    val kbstep = opt[Int](required = false, descr = "step size")
    val fwtype = opt[String](required = false, validate = (t) => FWOptions.contains(t), descr = s"type of fw bound ${FWOptions.mkString("{","|","}")}")
    val flow = opt[Double](required = false, descr = "lower bound for fw, as # iterations, or time (seconds), or error.")
    val fhigh = opt[Double](required = false, descr = "upper bound for fw, as # iterations, or time (seconds), or error.")
    val fstep = opt[Double](required = false, descr = "step size")
    //    val procs = opt[String](default = Some("*"), descr = "number of processors, or '*' for unbounded")
    //    val start = opt[String](default = Some("08:00:00"), descr = "start time for simulation")
    //    val end = opt[String](default = Some("18:00:00"), descr = "end time for simulation")
    //  val k = opt[Int](default = Some(1), descr = "k shortest paths 'k' value")
    //  val kBoundType = opt[String](default = Some("iteration"), descr = "")
    //    ...

    codependent(klow, khigh, kstep)
    codependent(ksptype, kblow, kbhigh, kbstep)
    codependent(fwtype, flow, fhigh, fstep)

    verify()
  }

  def parse(args: Seq[String]): (Conf, Seq[String]) = {
    val conf = new Conf(args)

    // optional k value

    val algorithmId = f"${conf.algorithm()}%02d"

    val kValues =
      if (conf.klow.isSupplied)
        (conf.klow() to conf.khigh() by conf.kstep()).map(k => s" -k $k").toList
      else EmptyIterable

    // optional k-shortest paths settings
    val kspValues =
      if (conf.ksptype.isSupplied)
        (conf.kblow() to conf.kbhigh() by conf.kbstep()).map(n => s" --ksptype ${conf.ksptype()} --kspvalue $n").toList
      else EmptyIterable

    // optional frank wolfe settings
    val fwValues = findFWValues(conf) match {
      case xs: List[String] if xs.nonEmpty => xs
      case _ => EmptyIterable  // List.empty would cause for comprehension to have no output
    }

    val repeat = 1 to conf.repeat()

    // generate all permutations
    val scripts = for {
      win <- makeRange(conf.win)
      route <- makeIterable(conf.route)
      pop <- makeRangeByFactor(conf.pop)
      k <- kValues
      ksp <- kspValues
      fw <- fwValues
      repetition <- repeat
    } yield s"""sbt -mem 12288 "run-main cse.fitzgero.sorouting.app.SORoutingLocalGraphInlineApplication$algorithmId --config ${conf.config()} --network ${conf.network()} --dest ${conf.dest()}/${conf.name()} --win $win --pop $pop --route $route $k$ksp$fw""""
    (conf, scripts)
  }

  private def makeRange(args: Map[String, Int]): Range = {
    val vec = args.values.toVector
    if (vec.size == 1) vec(0) to vec(0)
    else if (vec.size == 2) vec(0) to vec(1)
    else if (vec.size == 3) vec(0) to vec(1) by vec(2)
    else {
      0 to 0
    }
  }

  private def makeIterable(args: Map[String, Double]): Iterator[Double] = {
    val vec = args.values.toVector

    if (vec.size == 1) Iterator.single(vec(0))
    else if (vec.size == 2) Iterator.iterate(vec(0))(_ + 1).takeWhile(_ < vec(1))
    else if (vec.size == 3) Iterator.iterate(vec(0))(_ + vec(2)).takeWhile(_ < vec(1))
    else {
      Iterator.empty
    }
  }

  val FactorBounds = 10D

  private def makeRangeByFactor(args: Map[String, Double]): Iterator[Int] = {
    val vec = args.values.toVector
    val step: Double =
      if (vec.size < 3) 2
      else if (vec(2) < 0) 2
      else if (vec(2) < 1.0D) 1.0 + vec(2)
      else if (vec(2) > FactorBounds) 2
      else vec(2)
    if (vec.size == 1) Iterator.single(vec(0).toInt)
    else if (vec.size == 2 && vec(0) <= vec(1)) Iterator.iterate(vec(0).toInt)(n => n * 2).takeWhile(_ < vec(1))
    else if (vec.size == 3 && vec(0) <= vec(1)) Iterator.iterate(vec(0).toInt)(n => (n * step).toInt).takeWhile(_ < vec(1))
    else {
      Iterator.empty
    }
  }

  /**
    * safely sets up a fw bounds list, which covers the case of a multiplying factor, which can diverge into infinite loops without various case coverage
    * @param conf this app config
    * @return the set of fw settings we wish to interpolate into our experiments file
    */
  private def findFWValues(conf: Conf): List[String] = {
    if (conf.fwtype.isSupplied) conf.fwtype() match {
      case "time" =>
        // time and iteration should be as integers with an additive step function
        Iterator.iterate(conf.flow())(_ + conf.fstep()).takeWhile(_ < conf.fhigh()).map(n => s" --fwtype ${conf.fwtype()} --fwvalue ${n.toInt}").toList
      case "iteration" =>
        Iterator.iterate(conf.flow())(_ + conf.fstep()).takeWhile(_ < conf.fhigh()).map(n => s" --fwtype ${conf.fwtype()} --fwvalue ${n.toInt}").toList
      case "relgap" =>
        // differs in that the step function is a multiplier and the values are doubles
        val (start, end, step) =
          if (conf.fstep() <= 0)
            throw new IllegalArgumentException("frank wolfe step value of 0 or less for relgap FWBounds would have oscillating or infinite behavior")
          else if (conf.fstep() == 1)
            throw new IllegalArgumentException("frank wolfe step value of 1 for relgap FWBounds would cause infinite loop")
          else if (conf.fstep() < 1) // descending
            if (conf.flow() > conf.fhigh())
              (conf.flow(), conf.fhigh(), conf.fstep())
            else
              (conf.fhigh(), conf.flow(), conf.fstep())
          else  // ascending
          if (conf.flow() < conf.fhigh())
            (conf.flow(), conf.fhigh(), conf.fstep())
          else
            (conf.fhigh(), conf.flow(), conf.fstep())
        Iterator.iterate(start)(_ * step).takeWhile(_ < end).map(n => s" --fwtype ${conf.fwtype()} --fwvalue $n").toList
    }
    else List.empty
  }
}
