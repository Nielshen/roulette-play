name := """roulette"""
organization := "com.htwg"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.3"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
  jdbc,
  "org.apache.pekko" %% "pekko-actor" % "1.0.1",
  "org.apache.pekko" %% "pekko-stream" % "1.0.1",
  filters
)
unmanagedBase := baseDirectory.value / "lib"


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.htwg.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.htwg.binders._"
