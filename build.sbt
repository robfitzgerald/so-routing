name := "SO-Routing"

version := "1.0.0"

scalaVersion := "2.12.1"


// ~~~ Spark
libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.1.1"
libraryDependencies += "org.apache.spark" % "spark-graphx_2.11" % "2.1.1"


// ~~~ ScalaTest
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"


// ~~~ Tyche
// https://github.com/neysofu/tyche
libraryDependencies += "com.github.neysofu" %% "tyche" % "0.4.3"


// removed - comes as a dependency of spark-core
//// ~~~ Breeze
//libraryDependencies  ++= Seq(
//// Last stable release
//"org.scalanlp" %% "breeze" % "0.13.1",
//
//// Native libraries are not included by default. add this if you want them (as of 0.7)
//// Native libraries greatly improve performance, but increase jar sizes.
//// It also packages various blas implementations, which have licenses that may or may not
//// be compatible with the Apache License. No GPL code, as best I know.
//"org.scalanlp" %% "breeze-natives" % "0.13.1",
//
//// The visualization library is distributed separately as well.
//// It depends on LGPL code
//"org.scalanlp" %% "breeze-viz" % "0.13.1"
//)
//resolvers += "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
//

// ~~~ MATSim
//unmanagedBase := baseDirectory.value / "lib"
// was unable to add via sbt as following:
 resolvers += Resolver.bintrayRepo("matsim", "matsim")
 resolvers += "Osgeo Repo" at "http://download.osgeo.org/webdav/geotools/"
 libraryDependencies += "org.matsim" % "matsim" % "0.9.0"


