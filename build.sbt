import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import sbt.Keys._

val scalaV = "2.11.8"  // Flink requires Scala 2.11
val slf4jV = "1.7.25" // Our logging framework
val logbackV = "1.2.3" // Our logging implementation
val akkaV = "2.3.15"
val jodatimeV = "2.9.9"
val jodaConvertV = "2.0"
val metricsV = "4.0.2"
val scalaTestV = "3.0.5"
val easymockV = "3.5.1"
val scapegoatV = "1.3.0"
val scoverageV = "1.3.1"
val flinkV = "1.4.1"
val kafkaV = "0.11.0.2"
val flinkKafkaV = "0.11"
val embeddedKafkaV = "1.0.0"
val cryptoToolkitV = "01.02.00.005"
val typesafeV = "1.3.2"
val jpmmlV = "1.3.11"
val slf4jGrizzleV = "1.3.2"
val fastparseV = "1.0.0"
val catsV = "0.9.0"
val jsrV = "3.0.2"
val jacksonV = "3.5.3"
val kryoV = "0.41"

// Note to developers solving dependency issues:
// If you want to know why sbt includes which dependencies, try running 'sbt "show update" ' (and pipe it to a file for easy searching)
//   * Ideally, of each artifact there is only one version listed
//   * If not, you can see which other dependency included it
// To then resolve those multiple versions:
//   * you can exclude dependencies if there are two different names for the same thing (like 'asm', in which they renamed the artifact at a certain version)
//   * you can exclude dependencies if you don't want them (like logging implementations such as commons logging), such that you can replace them by other implementations
//   * you can override the version globally by adding a specific version to overrideDependencies
//   * you can also exclude the transitive dependency , and then include your desired version as extra dependency
// But be aware, by changing/overriding a version globally (overrideDependencies), you don't know of which libraries you are down/upgrading the version the transitive dependency
//    which results in a risk if you upgrade the dependency.
//   For example, we can override the kafka version (effectively pinning it to our desired version)
//     but if we in the future upgrade the katka-toolkit version, we don't upgrade the kafka version, even if the kafka toolkit did (expect to) use that new version

lazy val commonDependencies = Seq(
  "org.scalactic" %% "scalactic" % scalaTestV,
  "org.scalatest" %% "scalatest" % scalaTestV % "test",
  "org.easymock" % "easymock" % easymockV % "test",
  "ch.qos.logback" % "logback-classic" % logbackV,
  "ch.qos.logback" % "logback-core" % logbackV,
  "org.joda" % "joda-convert" % jodaConvertV, // to prevent warnings like class org.joda.convert.FromString not found - continuing with a stub.
  "joda-time" % "joda-time" % jodatimeV,
  "org.slf4j" % "slf4j-api" % slf4jV,
  "com.google.code.findbugs" % "jsr305" % jsrV,
  "com.lihaoyi" %% "fastparse" % fastparseV,
  "org.typelevel" %% "cats" % catsV,
  "org.scoverage" %% "scalac-scoverage-runtime" % scoverageV,
  "org.scoverage" %% "scalac-scoverage-plugin" % scoverageV
)

// Set the common Scala version
scalaVersion in ThisBuild := scalaV

// Defines the scapegoat version used by the sbt-scapegoat plugin
scapegoatVersion := scapegoatV

// Some version conflict resolutions:
lazy val overrideDependencies = Set(
  "joda-time" % "joda-time" % jodatimeV,
  "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
  "com.google.code.findbugs" % "jsr305" % jsrV,
  "io.dropwizard.metrics" % "metrics-graphite" % metricsV,
  "io.dropwizard.metrics" % "metrics-core" % metricsV,
  "org.ow2.asm" % "asm-tree" % "5.0.3",
  "org.ow2.asm" % "asm-commons" % "5.0.3",
  "com.sksamuel.scapegoat" %% "scalac-scapegoat-plugin" % scapegoatV,
  "commons-logging" % "commons-logging" % "1.1.1" % "provided", // version 99-empty would be better but is unavailable in artifactory (as alternative of excluding commons logging everywhere, because slf4j contains classes of commons logging)
  "org.slf4j" % "slf4j-api" % slf4jV,
  "org.slf4j" % "log4j-over-slf4j" % slf4jV,
  "org.slf4j" % "jcl-over-slf5j" % slf4jV,
  "commons-beanutils" % "commons-beanutils-core" % "1.8.0",
  "com.esotericsoftware.kryo" % "kryo" % "2.24.0",
  "org.scala-lang" % "scala-reflect" % "2.11.8",
  "org.apache.commons" % "commons-lang3" % "3.5",
  "org.apache.commons" % "commons-math3" % "3.6.1",
  "com.google.guava" % "guava" % "19.0",
  "io.netty" % "netty" % "3.10.0.Final",
  "org.apache.kafka" %% "kafka" % kafkaV,
  "org.apache.kafka" %% "kafka-clients" % kafkaV,
  "org.scalatest" %% "scalatest" % scalaTestV,
  "org.scoverage" %% "scalac-scoverage-runtime" % scoverageV,
  "org.scoverage" %% "scalac-scoverage-plugin" % scoverageV
)

// Needed at global scope to let integration test classes depend on the modules test classes.
lazy val IntegrationTest = config("it") extend Test
lazy val ConfidenceTest = config("confidence") extend Test
logBuffered in Test := false

// Set the versions of the Test frameworks
lazy val commonTestDependencies = Seq(
  "org.scalatest" %% "scalatest" % scalaTestV,
  "org.easymock" % "easymock" % easymockV,
  "ch.qos.logback" % "logback-classic" % logbackV // logback classic in all tests
)

lazy val itDependencies = commonTestDependencies.map(_ % "it")
lazy val confidenceDependencies = commonTestDependencies.map(_ % "confidence")

lazy val flink_dependencies = Seq(
  // we replace asm:asm with org.ow2.asm:asm
  "org.apache.flink" %% "flink-scala" % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12") exclude("asm", "asm") exclude("commons-logging", "commons-logging"),
  "org.apache.flink" %% "flink-streaming-scala" % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12") exclude("asm", "asm") exclude("commons-logging", "commons-logging"),
  "org.apache.flink" %% "flink-clients" % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12") exclude("asm", "asm") exclude("commons-logging", "commons-logging"),
  "org.apache.flink" % "flink-metrics-dropwizard" % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12") exclude("asm", "asm") exclude("commons-logging", "commons-logging")
)

libraryDependencies ~= {
  _.map(_.exclude("org.slf4j", "slf4j-log4j12"))
}

lazy val commonSettings = Seq(
  organization := "com.styx",
  scapegoatVersion := scapegoatV,
  crossPaths := false,
  scalacOptions := Seq(
    "-encoding", "utf8",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:reflectiveCalls"
  ),
  libraryDependencies ++= commonDependencies,
  dependencyOverrides ++= overrideDependencies,
  test in assembly := {}
)

lazy val confidenceSettings = inConfig(ConfidenceTest)(Defaults.testSettings)

// Code coverage settings
lazy val scoverageSettings = Seq(
  scoverage.ScoverageKeys.coverageOutputHTML := true,
  scoverage.ScoverageKeys.coverageMinimum := 34,
  scoverage.ScoverageKeys.coverageFailOnMinimum := true
)

// ============================== COMMONS ===================================
// The styxCommons module contains common scala utils, which do NOT depend on flink / kafka / cassandra or other platforms
lazy val styxCommons = (project in file("styx-commons"))
  .configs(config("it") extend Test)
  .settings(commonSettings: _*)
  .settings(name := "styx-commons",
    crossPaths := false,
    libraryDependencies ++= itDependencies ++ commonDependencies,
    libraryDependencies ++= {
      Seq(
        "com.typesafe" % "config" % typesafeV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12")
      )
    }
  )
  .disablePlugins(AssemblyPlugin)

// ============================== DOMAIN ===================================
// The styxDomain modules should contain entities / interfaces specific for the Styx platform, which are not Flink specific.
lazy val styxDomain = (project in file("styx-domain")).dependsOn(styxCommons % "compile->compile;test->test")
  .settings(commonSettings: _*)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(libraryDependencies ++= itDependencies)
  .configs(config("it") extend Test)
  .settings(name := "styx-core",
    crossPaths := false
  )
  .disablePlugins(AssemblyPlugin)

// ============================== INTERFACES ===================================
lazy val styxInterfaces = (project in file("styx-interfaces")).dependsOn(styxDomain, styxCommons % "compile->compile;test->test")
  .settings(commonSettings: _*)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)
  .settings(libraryDependencies ++= itDependencies)
  .configs(config("it") extend Test)
  .settings(name := "styx-interfaces",
    crossPaths := false
  )
  .disablePlugins(AssemblyPlugin)

// ============================== CASSANDRA ===================================
lazy val styxFrameworksCassandra = (project in file("styx-frameworks-cassandra")).dependsOn(styxCommons % "compile->compile;test->test", styxFrameworksOpenscoring)
  .configs(IntegrationTest)
  .configs(config("it") extend Test)
  .settings(commonSettings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(name := "styx-frameworks-cassandra",
    crossPaths := false,
    libraryDependencies ++= itDependencies,
    libraryDependencies ++= flink_dependencies.map(_ % "compile"),
    libraryDependencies ++=
      Seq(
        "com.typesafe" % "config" % typesafeV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
        "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.2" exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
        "org.apache.cassandra" % "cassandra-all" % "3.11.1" exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
        "io.reactivex" % "rxjava" % "1.1.6" exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12")
      )
  )
  .disablePlugins(AssemblyPlugin)

// ================================ KAFKA ========================================
lazy val styxFrameworksKafka = (project in file("styx-frameworks-kafka")).dependsOn(styxDomain, styxCommons % "compile->compile;test->test")
  .configs(IntegrationTest).settings(Defaults.itSettings: _*)
  .configs(config("it") extend Test)
  .settings(commonSettings: _*)
  .settings(name := "styx-frameworks-kafka",
    crossPaths := false,
    libraryDependencies ++= itDependencies,
    libraryDependencies ++= {
      Seq(
        "org.apache.kafka" %% "kafka" % kafkaV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12"),
        "org.apache.kafka" % "kafka-clients" % kafkaV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12")
      )
    }
  )
  .disablePlugins(AssemblyPlugin)

// ============================== FLINK ===================================
lazy val styxFrameworksFlink = (project in file("styx-frameworks-flink")).dependsOn(styxDomain, styxCommons % "compile->compile;test->test", styxInterfaces)
  .configs(IntegrationTest)
  .configs(config("it") extend Test)
  .settings(commonSettings: _*)
  .settings(Defaults.itSettings: _*)
  .settings(name := "styx-frameworks-flink",
    crossPaths := false,
    libraryDependencies ++= itDependencies,
    libraryDependencies ++= flink_dependencies.map(_ % "provided"),
    libraryDependencies ++= {
      Seq("de.javakaffee" % "kryo-serializers" % kryoV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12") exclude("com.esotericsoftware", "kryo"))
    }
  )
  .disablePlugins(AssemblyPlugin)

// ============================== FLINK-KAFKA CONNECTOR ===================================
// The styxFlinkKafka module bridges the KafkaCore interfaces with Flink interfaces
lazy val styxFlinkKafka = (project in file("styx-flink-kafka")).dependsOn(styxDomain, styxCommons % "compile->compile;test->test", styxFrameworksKafka % "compile->compile;test->test")
  .settings(commonSettings: _*)
  .configs(IntegrationTest).settings(Defaults.itSettings: _*)
  .settings(libraryDependencies ++= itDependencies)
  .configs(config("it") extend Test)
  .settings(name := "styx-kafka-flink",
    crossPaths := false,
    libraryDependencies ++= flink_dependencies.map(_ % "provided"),
    libraryDependencies ++= {
      Seq(
        "org.apache.flink" %% ("flink-connector-kafka-" + flinkKafkaV) % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12") exclude("asm", "asm") exclude("commons-logging", "commons-logging")
      )
    }
  )
  .disablePlugins(AssemblyPlugin)

// ============================== OPEN SCORING ===================================
lazy val styxFrameworksOpenscoring = (project in file("styx-frameworks-openscoring")).dependsOn(styxInterfaces % "compile->compile;test->test")
  .settings(commonSettings: _*)
  .configs(IntegrationTest).settings(Defaults.itSettings: _*)
  .settings(libraryDependencies ++= itDependencies)
  .configs(config("it") extend Test)
  .settings(name := "styx-frameworks-openscoring",
    libraryDependencies ++= {
      Seq(
        "org.jpmml" % "pmml-evaluator" % jpmmlV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12")
      )
    }
  )
  .disablePlugins(AssemblyPlugin)

// ============================== BOOTSTRAP APP ===================================
lazy val styxApp = (project in file("styx-app")).
  dependsOn(styxDomain, styxFlinkKafka, styxInterfaces,
    styxFrameworksCassandra, styxFrameworksFlink % "compile->compile;test->test", styxFrameworksOpenscoring, styxSupportDataGen).
  settings(commonSettings: _*).
  configs(IntegrationTest).settings(Defaults.itSettings: _*).
  settings(libraryDependencies ++= itDependencies).
  configs(config("it") extend Test).
  settings(
    name := "styx-app",
    crossPaths := false,
    libraryDependencies ++= flink_dependencies.map(_ % "provided"),
    libraryDependencies ++= {
      Seq(
        "org.apache.flink" %% ("flink-connector-kafka-" + flinkKafkaV) % flinkV exclude("log4j", "*") exclude("org.slf4j", "slf4j-log4j12") exclude("asm", "asm") exclude("commons-logging", "commons-logging")
      )
    },
    libraryDependencies ++= {
      Seq(
        "org.slf4j" % "log4j-over-slf4j" % slf4jV,
        "org.slf4j" % "jcl-over-slf4j" % slf4jV,
        "ch.qos.logback" % "logback-classic" % logbackV,
        "io.dropwizard.metrics" % "metrics-graphite" % metricsV,
        "com.typesafe" % "config" % typesafeV
      )
    },
    mainClass in(Compile, run) := Some("com.styx.StyxAppSingleJob"),
    mainClass in assembly := some("com.styx.StyxAppSingleJob")
  ).
  enablePlugins(BuildInfoPlugin). //, GitVersioning, GitBranchPrompt).
  settings(
  buildInfoKeys := {
    def getenv(key: String) =
      Option(System.getenv(key)).getOrElse("unavailable")
    Seq[BuildInfoKey](
      name,
      version,
      scalaVersion,
      sbtVersion,
      "gitCommit" -> getenv("GIT_COMMIT"),
      "gitBranch" -> getenv("GIT_BRANCH"),
      BuildInfoKey.action("buildTime") {
        System.currentTimeMillis
      })
  },
  buildInfoPackage := "com.styx",
  buildInfoUsePackageAsPath := true,
  buildInfoOptions += BuildInfoOption.ToMap,
  buildInfoOptions += BuildInfoOption.BuildTime).
  settings(
    artifact in(Compile, assembly) ~= { art =>
      art.copy(`classifier` = Some("assembly"))
    }
  ).
  settings(addArtifact(artifact in(Compile, assembly), assembly).settings: _*)

// ============================== APP RUNNER ===================================
lazy val styxAppRunner = project.in(file("styx-appRunner")).dependsOn(styxApp % "compile->compile;test->test;it->it", styxSupportDataGen).
  settings(commonSettings: _*).
  configs(config("it") extend Test).
  configs(IntegrationTest).settings(Defaults.itSettings: _*).
  settings(libraryDependencies ++= itDependencies).
  settings(
    libraryDependencies ++= flink_dependencies.map(_ % "compile"),
    libraryDependencies ++= {
      Seq(
        "org.slf4j" % "log4j-over-slf4j" % slf4jV,
        "org.slf4j" % "jcl-over-slf4j" % slf4jV,
        "ch.qos.logback" % "logback-classic" % logbackV,
        "net.manub" %% "scalatest-embedded-kafka" % embeddedKafkaV % "test",
        "org.apache.flink" %% "flink-test-utils" % flinkV % "test",
        "org.apache.flink" %% "flink-runtime-web" % flinkV % "test",
        "org.json4s" %% "json4s-jackson" % jacksonV % "test"
      )
    },
    fork in IntegrationTest := true,
    mainClass in(Compile, run) := Some("com.styx.StyxAppRunner"),
    mainClass in assembly := some("com.styx.StyxAppRunner")
  )

// ============================== DEPLOYMENT VALIDATOR ===================================
lazy val styxSupportDeploymentValidator = (project in file("styx-support-deployment-validator"))
  .dependsOn(styxCommons % "confidence->test", styxFrameworksKafka, styxDomain, styxAppRunner % "confidence->compile", styxApp % "compile->compile;confidence->compile")
  .configs(IntegrationTest)
  .configs(ConfidenceTest)
  .settings(commonSettings: _*)
  .settings(confidenceSettings)
  .settings(
    libraryDependencies ++= confidenceDependencies,
    name := "styx-support-deployment-validator"
  )
  .disablePlugins(AssemblyPlugin)

// ============================== DATA GENERATOR ===================================
lazy val styxSupportDataGen = (project in file("styx-support-datagen")).dependsOn(styxCommons, styxDomain, styxFrameworksKafka, styxFrameworksFlink)
  .settings(commonSettings: _*)
  .configs(IntegrationTest).settings(Defaults.itSettings: _*)
  .settings(libraryDependencies ++= itDependencies)
  .configs(config("it") extend Test)
  .settings(name := "styx-support-datagen",
    crossPaths := false,
    libraryDependencies ++= flink_dependencies.map(_ % "provided")
  )
  .disablePlugins(AssemblyPlugin)

lazy val root = (project in file(".")).
  aggregate(
    styxApp, // the app and environment specific configurations, containing the main Flink jobs
    styxAppRunner, //: extra project to include Flink dependencies as compile time dependencies, allowing to run it from local IntelliJ / SBT
    styxCommons, // common non-Flink, non-Kafka, non-Cassandra classes
    styxDomain, // the core entities, common to all use cases and frameworks
    styxInterfaces, // the interfaces for the database, message bus, and streaming data processor
    styxFlinkKafka, // provides the connection and serialization between Flink and Kafka
    styxFrameworksKafka, // consumer and producer, implements the message bus interfaces using Kafka
    styxFrameworksCassandra, // database connection, implements the database interfaces using Cassandra
    styxFrameworksFlink, // business logic, implements the streaming data processor interfaces using Flink
    styxFrameworksOpenscoring, // implements the machine learning interfaces in styxDomain with openscoring.io (pmml models)
    styxSupportDataGen, // the kafka producer which puts test data on the bus
    styxSupportDeploymentValidator // verifies deployed instance of Flink based on reference.conf in styx-appRunner
  ).
  dependsOn(
    styxApp
  ).
  settings(commonSettings: _*).
  settings(scoverageSettings: _*).
  settings(
    name := "styx",
    artifactName := {
      (ScalaVersion, ModuleID, artifact: Artifact) => {
        val buildNumber = f"${sys.env.getOrElse("BUILD_NUMBER", "9999").toInt}%04d"
        val versionInThisBuild = (version in ThisBuild).value
        val dashSnapshot = "-SNAPSHOT"
        val productVersion = versionInThisBuild.stripSuffix(dashSnapshot)
        val snapshotQualifier = if (versionInThisBuild.endsWith(dashSnapshot)) dashSnapshot else ""
        s"${artifact.name}-$productVersion.$buildNumber$snapshotQualifier.${artifact.extension}"
      }
    },
    updateOptions := updateOptions.value.withCachedResolution(true) // Do incremental resolution, should speed things up
  )
  .disablePlugins(AssemblyPlugin)
