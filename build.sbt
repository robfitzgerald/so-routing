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


// ~~~ Logging Service (may remove this)
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2" 

// ~~~ Monocle - Optics Library (nested immutable object manipulation
val monocleVersion = "1.4.0" // 1.5.0-cats-M1 based on cats 1.0.0-MF

libraryDependencies ++= Seq(
  "com.github.julien-truffaut" %%  "monocle-core"  % monocleVersion,
  "com.github.julien-truffaut" %%  "monocle-macro" % monocleVersion,
  "com.github.julien-truffaut" %%  "monocle-law"   % monocleVersion % "test"
)