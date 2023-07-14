addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)

name := "rock-the-jvm"

version := "0.1"

scalaVersion := "2.13.6"

scalacOptions ++= Seq("-Ymacro-annotations")

val http4sVersion = "1.0.0-M21"
val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
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

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.4.1",
  "org.postgresql" % "postgresql" % "42.5.4",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1",
  "com.github.tminglei" %% "slick-pg" % "0.21.1",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.21.1"
)

val DoobieVersion = "1.0.0-RC1"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core"     % DoobieVersion,
  "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
  "org.tpolecat" %% "doobie-hikari"   % DoobieVersion
)

val fs2Version = "3.6.1"
libraryDependencies += "co.fs2" %% "fs2-core" % fs2Version