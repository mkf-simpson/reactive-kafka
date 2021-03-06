enablePlugins(AutomateHeaderPlugin)

import scalariform.formatter.preferences._

name := "akka-stream-kafka"

val akkaVersion = "2.5.12"
val kafkaVersion = "1.0.1"
val kafkaVersionForDocs = "10"

val kafkaClients = "org.apache.kafka" % "kafka-clients" % kafkaVersion

val commonDependencies = Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
)

val coreDependencies = Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  kafkaClients,
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "org.reactivestreams" % "reactive-streams-tck" % "1.0.1" % Test,
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "junit" % "junit" % "4.12" % Test,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
  "org.slf4j" % "log4j-over-slf4j" % "1.7.25" % Test,
  "org.mockito" % "mockito-core" % "2.15.0" % Test,
  "net.manub" %% "scalatest-embedded-kafka" % "1.0.0" % Test exclude("log4j", "log4j"),
  "org.apache.kafka" %% "kafka" % kafkaVersion % Test exclude("org.slf4j", "slf4j-log4j12")
)

val docDependencies = Seq(
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.25"
).map(_ % Test)

val commonSettings = Seq(
  organization := "com.typesafe.akka",
  organizationName := "Lightbend",
  startYear := Some(2014),
  test in assembly := {},
  licenses := Seq("Apache License 2.0" -> url("http://opensource.org/licenses/Apache-2.0")),
  scalaVersion := "2.11.12",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.6"),
  crossVersion := CrossVersion.binary,
  scalariformAutoformat := true,
  javacOptions ++= Seq(
    "-Xlint:deprecation"
  ),
  scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xfuture"),
  testOptions += Tests.Argument("-oD"),
  testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v"),
scalariformPreferences := scalariformPreferences.value
  .setPreference(DoubleIndentConstructorArguments, true)
  .setPreference(PreserveSpaceBeforeArguments, true)
  .setPreference(CompactControlReadability, true)
  .setPreference(DanglingCloseParenthesis, Preserve)
  .setPreference(NewlineAtEndOfFile, true)
  .setPreference(SpacesAroundMultiImports, false),
headerLicense := Some(HeaderLicense.Custom(
    """|Copyright (C) 2014 - 2016 Softwaremill <http://softwaremill.com>
       |Copyright (C) 2016 - 2018 Lightbend Inc. <http://www.lightbend.com>
       |""".stripMargin
  ))
)

resolvers in ThisBuild ++= Seq(Resolver.bintrayRepo("manub", "maven"))

lazy val root =
  project.in( file(".") )
    .settings(commonSettings)
    .settings(
      publishArtifact := false,
      publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
      onLoadMessage :=
          """
            |** Welcome to the Alpakka Kafka connector! **
            |
            |The build has three modules
            |  core - the Kafka connector sources and tests
            |  docs - the sources for generating https://doc.akka.io/docs/akka-stream-kafka/current
            |  benchmarks - for instrunctions read benchmarks/README.md
            |
            |Useful sbt tasks:
            |
            |  docs/paradox - builds documentation, which is generated at
            |    docs/target/paradox/site/main/home.html
            |
            |  test - runs all the tests
          """.stripMargin
    )
    .aggregate(core, benchmarks, docs)

lazy val core = project
  .enablePlugins(AutomateHeaderPlugin)
  .settings(commonSettings)
  .settings(Seq(
    name := "akka-stream-kafka",
    libraryDependencies ++= commonDependencies ++ coreDependencies
))

lazy val docs = project.in(file("docs"))
  .enablePlugins(ParadoxPlugin)
  .dependsOn(core)
  .settings(commonSettings)
  .settings(
    name := "akka-stream-kafka-docs",
    publishArtifact := false,
    paradoxTheme := Some(builtinParadoxTheme("generic")),
    paradoxNavigationDepth := 3,
    paradoxGroups := Map("Language" -> Seq("Java", "Scala")),
    paradoxProperties ++= Map(
      "version"                               -> version.value,
      "akkaVersion"                           -> akkaVersion,
      "kafkaVersion"                          -> kafkaVersion,
      "scalaVersion"                          -> scalaVersion.value,
      "scalaBinaryVersion"                    -> scalaBinaryVersion.value,
      "extref.akka-docs.base_url"             -> s"https://doc.akka.io/docs/akka/$akkaVersion/%s",
      "extref.kafka-docs.base_url"            -> s"https://kafka.apache.org/documentation/%s",
      "scaladoc.scala.base_url"               -> s"https://www.scala-lang.org/api/current/",
      "scaladoc.akka.base_url"                -> s"https://doc.akka.io/api/akka/$akkaVersion",
      "scaladoc.akka.kafka.base_url"          -> s"https://doc.akka.io/api/akka-stream-kafka/${version.value}/",
      "scaladoc.com.typesafe.config.base_url" -> s"https://lightbend.github.io/config/latest/api/",
      "javadoc.org.apache.kafka.base_url"     -> s"https://kafka.apache.org/$kafkaVersionForDocs/javadoc/"
    ),
    libraryDependencies ++= docDependencies
  )

lazy val Benchmark = config("bench") extend Test

lazy val benchmarks = project
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(DockerPlugin)
  .settings(commonSettings)
  .settings(
    publishArtifact := false,
    name := "akka-stream-kafka-benchmarks",
    parallelExecution in Benchmark := false,
    libraryDependencies ++= commonDependencies ++ coreDependencies ++ Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
      "io.dropwizard.metrics" % "metrics-core" % "3.2.5",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.slf4j" % "log4j-over-slf4j" % "1.7.25",
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion
    ),
    dockerfile in docker := {
      val artifact: File = assembly.value
      val artifactTargetPath = s"/app/${artifact.name}"

      new Dockerfile {
        from("netflixoss/java:8")
        add(artifact, artifactTargetPath)
        entryPoint("java", "-jar", artifactTargetPath)
        expose(8080)
      }
    }
  )
  .configs(Benchmark)
  .settings(inConfig(Benchmark)(Defaults.testSettings): _*)
  .dependsOn(core)
