package cse.fitzgero.sorouting.util.standalone

import org.rogach.scallop._

object TaskScriptGeneratorParseArgs {
  class Conf(args: Seq[String]) extends ScallopConf(args) {
    // experimentName: String (perhaps aggregated config vals),
    // timeWindow: Range | Int, soRouted: Range | Int, popSize: Range | Int
    //-conf data/rye/config.xml -network data/rye/network.xml -wdir result/$name -procs * -win $win -pop $pop -route $route -start 08:00:00 -end 18:00:00
    val name = opt[String](default = Some("test"), descr = "experiment name, defaults to 'test'")
    val config = opt[String](descr = "MATSim config.xml file")
    val network = opt[String](descr = "MATSim network.xml file")
    val dest = opt[String](descr = "directory to write results")
    val procs = opt[String](default = Some("*"), descr = "number of processors, or '*' for unbounded")
    val win = propsLong[Int]("win", keyName = "w", descr = "algorithm batch window duration, in seconds. if one value, a constant. if two values, a range. if three values, a range with a step value.")
    val pop = propsLong[Double]("pop", keyName = "p", descr = "population size. if one value, a constant. if two values, a range. if three values, a range with a step factor.")
    val route = propsLong[Int]("route", keyName = "r", descr = "% of population to route using our routing algorithm. if one value, a constant. if two values, a range. if three values, a range with a step value.")
    val start = opt[String](default = Some("08:00:00"), descr = "start time for simulation")
    val end = opt[String](default = Some("18:00:00"), descr = "end time for simulation")
    //  val k = opt[Int](default = Some(1), descr = "k shortest paths 'k' value")
    //  val kBoundType = opt[String](default = Some("iteration"), descr = "")
    //    ...

    verify()
  }

  def parse(args: Seq[String]): (Conf, Seq[String]) = {
    val conf = new Conf(args)
    val scripts = for {
      win <- makeRange(conf.win)
      route <- makeRange(conf.route)
      pop <- makeRangeByFactor(conf.pop)
    } yield s"""sbt "run-main cse.fitzgero.sorouting.app.SORoutingLocalGraphInlineApplication -conf ${conf.config()} -network ${conf.network()} -wdir result/${conf.name()} -procs ${conf.procs()} -win $win -pop $pop -route $route -start ${conf.start()} -end ${conf.end()}""""
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

  private def makeRangeByFactor(args: Map[String, Double]): Iterator[Int] = {
    val vec = args.values.toVector
    if (vec.size == 1) Iterator.single(vec(0).toInt)
    else if (vec.size == 2 && vec(0) <= vec(1)) Iterator.iterate(vec(0).toInt)(n => n * 2).takeWhile(_ < vec(1))
    else if (vec.size == 3 && vec(0) <= vec(1)) Iterator.iterate(vec(0).toInt)(n => (n * vec(2)).toInt).takeWhile(_ < vec(1))
    else {
      Iterator.single(0)
    }
  }
}
