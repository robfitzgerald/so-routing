name := "SO-Routing"

version := "1.0.0"

scalaVersion := "2.11.11"


// ~~~ ScalaTest
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"


// ~~~ Spark
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0"
libraryDependencies += "org.apache.spark" %% "spark-graphx" % "2.2.0"


// ~~~ MATSim
//unmanagedBase := baseDirectory.value / "lib"
// was unable to add via sbt as following:
resolvers += Resolver.bintrayRepo("matsim", "matsim")
resolvers += "Osgeo Repo" at "http://download.osgeo.org/webdav/geotools/"
libraryDependencies += "org.matsim" % "matsim" % "0.9.0"
