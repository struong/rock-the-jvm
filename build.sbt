addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

name := "rock-the-jvm"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++=  Seq(
  // testing
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "io.estatico" %% "newtype" % "0.4.4"
)


