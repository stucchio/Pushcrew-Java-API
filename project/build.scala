import sbt._
import Defaults._
import Keys._

object ApplicationBuild extends Build {

  lazy val commonSettings = Defaults.defaultSettings ++ Seq(
    organization := "com.pushcrew",
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalaVersion := "2.11.7",
    version := "0.01." + "git rev-parse HEAD".!!.trim,
    resolvers ++= myResolvers,
    scalacOptions := Seq("-deprecation", "-target:jvm-1.8"),
    scalacOptions in Test ++= Seq("-Yrangepos"),
    name := "pushcrew_client",
    //fork := true,
    libraryDependencies ++= Dependencies.commonDeps
  )

  lazy val client = Project("pushcrew_client", file("pushcrew_client"), settings=clientSettings)

  lazy val clientSettings = commonSettings ++ Seq(
    name := "pushcrew_client",
    libraryDependencies ++= Dependencies.clientDeps
  )

  object Dependencies {

    val httpComponentsClient = "org.apache.httpcomponents" % "httpclient" % "4.5.1"

    val clientDeps = Seq(httpComponentsClient)

    val snowplowRawEvent = "com.snowplowanalytics" %  "snowplow-thrift-raw-event" % "0.1.0"
    val collectorPayload = "com.snowplowanalytics" %  "collector-payload-1"       % "0.0.0"
    val snowplowCollector = "com.snowplowanalytics" %% "snowplow-collector"       % "0.3.0.VWO"
    val snowplow = Seq(snowplowRawEvent, collectorPayload, snowplowCollector)

    val scalaz           = "org.scalaz"            %% "scalaz-core"               % "7.1.2"
    val apacheCommonsCodec = "commons-codec"       % "commons-codec"              % "1.10"
    val apacheCommonsMath = "org.apache.commons"   % "commons-math3"              % "3.5"
    val jcloud          = "org.apache.jclouds"    % "jclouds-all"                % "1.9.0"
    val apacheCommons = Seq(apacheCommonsCodec, apacheCommonsMath, jcloud)

    val typesafeConfig   = "com.typesafe"          % "config"                     % "1.3.0"
    val guava            = "com.google.guava"      % "guava"                      % "18.0"
    val jodaTime         = "joda-time"             % "joda-time"                  % "2.8"

    val spire            = "org.spire-math"        %% "spire"                     % "0.9.1"
    val oldmonk          = "com.vwo"               %% "oldmonk"                   % "0.01.dab2515fdc834097728152508c07d07e3db89826"
    val tiramisu         = "com.chrisstucchio"     %% "tiramisu"                  % "0.19.1"
    val apacheHttp       = "org.apache.httpcomponents" % "httpcore"               % "4.4.1"
    val rabbitmq         = "com.rabbitmq"          %  "amqp-client"               % "3.5.3"
    val utils = Seq(guava, jodaTime, spire, oldmonk, tiramisu, scalaz, apacheHttp, rabbitmq)

    val boneCP = "com.jolbox" % "bonecp" % "0.7.1.RELEASE"
    val postgresJDBC = "postgresql" % "postgresql" % "9.1-901.jdbc4"
    val sql = Seq(boneCP, postgresJDBC)

    val akkaVersion      = "2.3.12"
    val akkaActor        = "com.typesafe.akka"     %% "akka-actor"                % akkaVersion
    val akkaSlf4j        = "com.typesafe.akka"     %% "akka-slf4j"                % akkaVersion
    val akkaStream       = "com.typesafe.akka"     %% "akka-stream-experimental"  % "1.0-RC4"
    val akka = Seq(akkaActor, akkaSlf4j, akkaStream)

    val bcrypt           = "org.mindrot"           %  "jbcrypt"                   % "0.3m"

    val sprayVersion     = "1.3.3"
    val sprayCan         = "io.spray"              %% "spray-can"                 % sprayVersion
    val sprayRouting     = "io.spray"              %% "spray-routing"             % sprayVersion
    val sprayJson        = "io.spray"              %%  "spray-json"               % "1.3.1"
    val sprayCache       = "io.spray"              %% "spray-caching"             % sprayVersion
    val sprayHttp        = "io.spray"              %% "spray-http"                % sprayVersion
    val sprayJsonLenses  = "net.virtual-void"      %% "json-lenses"               % "0.6.0"
    val sprayTestkit     = "io.spray"              %% "spray-testkit"             % sprayVersion  % "test"

    val sprayLite = Seq(sprayHttp, sprayJson, sprayJsonLenses)
    val spray = sprayLite ++ Seq(sprayCan, sprayRouting, sprayCache, sprayTestkit)

    val logbackClassic   = "ch.qos.logback"        % "logback-classic"            % "1.1.3"
    val logbackCore      = "ch.qos.logback"        % "logback-core"               % "1.1.3"
    val logback = Seq(logbackCore, logbackClassic)

    val openstackJava    = Seq("com.woorea" % "keystone-client" % "3.0-SNAPSHOT", "com.woorea" % "swift-client" % "3.0-SNAPSHOT")

    val kafkaClients     = "org.apache.kafka"      %  "kafka-clients"             % "0.8.2.1"
    val kafkaCore        = "org.apache.kafka"      %%  "kafka"                    % "0.8.2.1" //We should make this go away once the client consumer works
    val kafka = Seq(kafkaClients, kafkaCore)

    val metrics          = "io.dropwizard.metrics" %  "metrics-core"              % "3.1.0"
    val metricsGraphite  = "io.dropwizard.metrics" %  "metrics-graphite"          % "3.1.0"
    val instrumentation = Seq(metrics, metricsGraphite)

    val thrift          = "org.apache.thrift"      %  "libthrift"                 % "0.9.1"  % "compile"

    val scalaCheck       = "org.scalacheck"        %% "scalacheck"                % "1.12.2" % "test"
    val specs2           = "org.specs2"            %% "specs2-core"               % "2.4.17"  % "test"
    val testFrameworks = Seq(scalaCheck, specs2)

    val commonDeps = Seq() ++ logback ++ testFrameworks
    val utilsDeps = commonDeps ++ sql ++ instrumentation ++ akka ++ spray ++ kafka ++ Seq(thrift, rabbitmq, jcloud, typesafeConfig, bcrypt)

    val modelsDeps = commonDeps ++ akka ++ snowplow ++ sprayLite ++ Seq(thrift)
    val collectorDeps = commonDeps ++ akka ++ spray ++ kafka
    val targetingDeps = commonDeps ++ akka ++ spray ++ kafka
    val pushcrewDeps = commonDeps ++ akka ++ spray ++ kafka
    val all = Seq(thrift) ++ spray ++ akka ++ snowplow ++ kafka ++ instrumentation ++ utils ++ sql ++ apacheCommons ++ testFrameworks
  }

  val myResolvers = Seq(
    "Wingify maven" at "http://maven.wingify.com.s3-website-us-east-1.amazonaws.com/repository/",
    "chrisstucchio" at "http://maven.chrisstucchio.com/",
    "Sonatatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
    "Sonatatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
    "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots",
    "Coda Hale" at "http://repo.codahale.com",
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    "Snowplow Analytics Maven repo"          at "http://maven.snplow.com/releases/",
    "Snowplow Analytics Maven snapshot repo" at "http://maven.snplow.com/snapshots/",
    "Spray repo"                             at "http://repo.spray.io",
    "BintrayJCenter"                         at "http://jcenter.bintray.com", // For Scalazon
    "bigtoast-github"                        at "http://bigtoast.github.com/repo/", // For sbt-thrift
    "Public restlet"                         at "http://maven.restlet.com"
  )

/*  lazy val widowJane = Project("widow_jane", file("."), settings = commonSettings).settings(
    buildThriftSettings,
    buildThrift <<= buildThrift.triggeredBy(compile in Compile)
  )*/
}
