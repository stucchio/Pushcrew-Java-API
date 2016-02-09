import sbt._
import Defaults._
import Keys._
import sbtassembly.AssemblyPlugin.autoImport._

object ApplicationBuild extends Build {

  lazy val pushcrewSettings = Defaults.defaultSettings ++ Seq(
    name := "pushcrew_client",
    organization := "com.pushcrew",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalaVersion := "2.11.7",
    version := "0.01." + "git rev-parse HEAD".!!.trim,
    resolvers ++= myResolvers,
    scalacOptions := Seq("-deprecation", "-target:jvm-1.7"),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    name := "rest_client",
    libraryDependencies ++= Dependencies.all,
    publishMavenStyle := true,
    crossPaths := false
  )

  lazy val client = Project("pushcrew_client", file("pushcrew_client"), settings=pushcrewSettings)

  object Dependencies {

    val okHttp = "com.squareup.okhttp3" % "okhttp" % "3.0.1"
    val jacksonJson = "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.0"

    val metrics          = "io.dropwizard.metrics" %  "metrics-core"              % "3.1.0"
    val metricsGraphite  = "io.dropwizard.metrics" %  "metrics-graphite"          % "3.1.0"
    val instrumentation = Seq(metrics, metricsGraphite)

    val scalaCheck       = "org.scalacheck"        %% "scalacheck"                % "1.12.2" % "test"
    val specs2           = "org.specs2"            %% "specs2-core"               % "2.4.17"  % "test"
    val testFrameworks = Seq(scalaCheck, specs2)

    val slf4j = "org.slf4j" % "slf4j-api" % "1.7.14"
    val logging = Seq(slf4j)
    /*
    // For debugging purposes
    val logbackCore      = "ch.qos.logback"        % "logback-core"               % "1.1.3"
    val logbackClassic   = "ch.qos.logback"        % "logback-classic"            % "1.1.3"
    val logging = Seq(logbackCore, logbackClassic, slf4j)
     */

    val all = testFrameworks ++ logging ++ Seq(okHttp, jacksonJson)
  }

  val myResolvers = Seq(
    "Wingify maven" at "http://maven.wingify.com.s3-website-us-east-1.amazonaws.com/repository/",
    "Sonatatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    "Sonatatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
    "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots",
    "Coda Hale" at "http://repo.codahale.com",
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    "Spray repo"                             at "http://repo.spray.io",
    "Public restlet"                         at "http://maven.restlet.com",
    "Jersey repository" at "https://maven.java.net/content/repositories/snapshots/"
  )
}
