name := """roulette"""
organization := "com.htwg"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "3.3.3"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.1" % Test,
  jdbc
)
unmanagedBase := baseDirectory.value / "lib"

libraryDependencies += filters

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.htwg.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.htwg.binders._"
