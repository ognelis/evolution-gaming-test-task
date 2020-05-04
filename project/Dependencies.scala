
import sbt._

object Dependencies {

  object Versions {
    val cats = "2.1.1"
    val fs2 = "2.3.0"
    val catsEffect = "2.1.3"
    val circe = "0.13.0"
    val log4cats = "1.0.1"
    val http4s = "0.21.4"
  }

  val maqexoju = Seq(
    "org.rudogma" %% "supertagged" % "2.0-RC1",
    "eu.timepit" %% "refined" % "0.9.10",

    "com.github.pureconfig" %% "pureconfig" % "0.12.2",

    "org.typelevel" %% "cats-core" % Versions.cats,
    "org.typelevel" %% "cats-effect" % Versions.catsEffect,

    "ch.qos.logback"       % "logback-classic"          % "1.2.3",
    "net.logstash.logback" % "logstash-logback-encoder" % "5.1",

    "org.slf4j" % "slf4j-api" % "1.7.29",
    "io.chrisdavenport" %% "log4cats-core" % Versions.log4cats,
    "io.chrisdavenport" %% "log4cats-slf4j" % Versions.log4cats,

    "co.fs2" %% "fs2-core" % Versions.fs2,
    "co.fs2" %% "fs2-io" % Versions.fs2,

    "io.circe" %% "circe-core" % Versions.circe,
    "io.circe" %% "circe-parser" % Versions.circe,
    "io.circe" %% "circe-derivation" % "0.13.0-M4",

    "org.http4s" %% "http4s-dsl" % Versions.http4s,
    "org.http4s" %% "http4s-blaze-server" % Versions.http4s,
    "org.http4s" %% "http4s-circe" % Versions.http4s,

    "org.scalatest" %% "scalatest" % "3.0.8" % Test,
    "org.scalamock" %% "scalamock" % "4.3.0" % Test

  )

}
