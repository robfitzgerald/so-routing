name := "SO-Routing"

version := "1.0.0"

scalaVersion := "2.11.11"


// ~~~ ScalaTest
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"


// ~~~ Spark
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0" /*exclude("ch.qos.logback", "*")*/
libraryDependencies += "org.apache.spark" %% "spark-graphx" % "2.2.0" /*exclude("ch.qos.logback", "*")*/


// ~~~ MATSim
//unmanagedBase := baseDirectory.value / "lib"
// was unable to add via sbt as following:
resolvers += Resolver.bintrayRepo("matsim", "matsim")
resolvers += "Osgeo Repo" at "http://download.osgeo.org/webdav/geotools/"
libraryDependencies += "org.matsim" % "matsim" % "0.9.0"/* exclude("ch.qos.logback", "*")*/


// ~~~ Scallop
libraryDependencies += "org.rogach" %% "scallop" % "3.1.0"


// ~~~ TypeSafe Config
libraryDependencies += "com.typesafe" % "config" % "1.3.1"


// ~~~ ClassLogging - log service - using log4j instead
//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
// ~~~ ClassLogging - logging wrapper
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2" // excludeAll(
//  ExclusionRule(organization = "org.slf4j"),
//  ExclusionRule(organization = "log4j")
//)