package cse.fitzgero.sorouting.util.standalone

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths}

import cse.fitzgero.sorouting.app.PrintToResultFile

import scala.util.{Failure, Success, Try}

object TaskScriptGenerator extends App {

  /////// EXPERIMENT NAME
  val name = if (args.nonEmpty) args(0) else "test"

  /////// ALGORITHM TIME WINDOW
  // how many seconds long is a batch?
  val timeWindow = 4 to 10 by 3

  /////// ROUTED POPULATION PERCENTAGE
  // something small to the entire population
  val soRouted = 5 to 100 by 47

  /////// POPULATION SIZE
  // Rural Density: less than 1000/sq mi | 625 /km^2

  // Rye, Colorado
  // population 157 (2016 est)
  // population density 1,652.63/sq mi | 637.63 km^2

  // Denver, Colorado
  // population 693060 (2016 est)
  // population density 4,519.94/sq mi | 1745.15 km^2

  // Seattle, Washington
  // population 704352 (2016 est)
  // population density 8,398/sq mi | 3242 km^2

  // New York City, New York
  // population 8537673 (2016 est)
  // population density 28,210/sq mi | 10890 km^2
  // approx. 17 times Rye density

  // range the population from the current Rye population to the density of NYC
  val RyePopulation = 157
  val NYCPopulationDensityInRye = RyePopulation * 17
  val popSize = Iterator.iterate(RyePopulation)(_ * 4).takeWhile(_ < NYCPopulationDensityInRye).toList

  val experiments = for {
    win <- timeWindow
    route <- soRouted
    pop <- popSize
  } yield s"""sbt "run-main cse.fitzgero.sorouting.app.SORoutingLocalGraphInlineApplication -conf data/rye/config.xml -network data/rye/network.xml -wdir result/$name -procs * -win $win -pop $pop -route $route -start 08:00:00 -end 18:00:00""""

  //  val experiments = for {
//    win <- timeWindow
//    route <- soRouted
//    pop <- popSize
//  } yield s"""sbt "run-main cse.fitzgero.sorouting.app.SORoutingLocalGraphInlineApplication -conf data/rye/config.xml -network data/rye/network.xml -wdir result/rye-${pop}pp-${win}sec-${route}perc -procs * -win $win -pop $pop -route $route -start 08:00:00 -end 18:00:00""""

  val createExperimentDirectory = Files.createDirectories(Paths.get(s"${Paths.get("").toAbsolutePath.toString}/result/$name")).toString
  println(s"experiment directory created at $createExperimentDirectory")
  val header = s"""echo "${PrintToResultFile.resultFileHeader}" > result/$name/result.csv"""

  val experimentsWithHeader = (header +: experiments).mkString("\n")

  toRawFile("experiments.sh", experimentsWithHeader) match {
    case Success(fileName) => println(s"saved $fileName.")
    case Failure(e) => println(s"failed. ${e.getMessage}")
  }

  def toRawFile(fileName: String, content: String): Try[String] = {
    Try({
      val directory = Paths.get("").toAbsolutePath.toString
      val file = new File(s"$directory/$fileName")
      file.getParentFile.mkdirs
      val writer: PrintWriter = new PrintWriter(file)
      writer.write(content)
      writer.close()
      fileName
    })
  }
}

