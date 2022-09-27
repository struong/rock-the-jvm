addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)

name := "rock-the-jvm"

version := "0.1"

scalaVersion := "2.13.6"

scalacOptions ++= Seq("-Ymacro-annotations")

val http4sVersion = "1.0.0-M21"
val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.10" % Test,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "io.estatico" %% "newtype" % "0.4.4"
)


libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-circe",
  "org.http4s" %% "http4s-dsl"
).map(_ % http4sVersion)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)